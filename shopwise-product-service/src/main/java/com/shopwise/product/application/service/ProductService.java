package com.shopwise.product.application.service;

import com.shopwise.product.application.dto.*;
import com.shopwise.product.application.port.ProductUseCase;
import com.shopwise.product.domain.model.Product;
import com.shopwise.product.domain.port.ProductRepositoryPort;
import com.shopwise.product.infrastructure.client.UserClient;
import com.shopwise.product.infrastructure.client.dto.UserClientResponse;
import com.shopwise.product.infrastructure.exception.BusinessException;
import com.shopwise.product.infrastructure.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements ProductUseCase {

    private final ProductRepositoryPort productRepositoryPort;
    private final UserClient userClient;

    @Override
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = Product.create(
                request.name(),
                request.description(),
                request.price(),
                request.stock(),
                request.category()
        );
        product = setUpdatedBy(product);
        return buildProductResponse(productRepositoryPort.save(product));
    }

    @Override
    public ProductResponse getProductById(Long id) {
        return buildProductResponse(findProductById(id));
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepositoryPort.findAll()
                .stream()
                .map(this::buildProductResponse)
                .toList();
    }

    @Override
    public List<ProductResponse> getProductsByCategory(String category) {
        return productRepositoryPort.findByCategory(category)
                .stream()
                .map(this::buildProductResponse)
                .toList();
    }

    @Override
    public ProductResponse increaseStock(Long id, UpdateStockRequest request) {
        Product product = findProductById(id);
        product.increaseStock(request.quantity());
        product = setUpdatedBy(product);
        return buildProductResponse(productRepositoryPort.save(product));
    }

    @Override
    public void deleteProduct(Long id) {
        findProductById(id);
        productRepositoryPort.deleteById(id);
    }

    @Override
    @Transactional
    public void reserveStock(Long productId, Integer quantity) {
        Product product = findProductById(productId);
        product.reserve(quantity);
        productRepositoryPort.save(product);
        log.info("Stok rezerve edildi — productId: {}, quantity: {}", productId, quantity);
    }

    @Override
    @Transactional
    public void releaseReservation(Long productId, Integer quantity) {
        Product product = findProductById(productId);
        product.releaseReservation(quantity);
        productRepositoryPort.save(product);
        log.info("Stok rezervi kaldırıldı — productId: {}, quantity: {}", productId, quantity);
    }

    @Override
    @Transactional
    public void confirmReservation(Long productId, Integer quantity) {
        Product product = findProductById(productId);
        product.confirmReservation(quantity);
        productRepositoryPort.save(product);
        log.info("Stok rezervi onaylandı — productId: {}, quantity: {}", productId, quantity);
    }

    private Product findProductById(Long id) {
        return productRepositoryPort.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
                        "Ürün bulunamadı: " + id));
    }

    private ProductResponse buildProductResponse(Product product) {
        return ProductResponse.from(
                product,
                getAuditUser(product.getCreatedBy()),
                getAuditUser(product.getUpdatedBy())
        );
    }

    private AuditUserResponse getAuditUser(Long userId) {
        if (userId == null) return null;
        try {
            String token = getCurrentToken();
            UserClientResponse user = userClient.getUserById(userId, token);
            return new AuditUserResponse(user.id(), user.fullName());
        } catch (Exception e) {
            log.warn("Kullanıcı bilgisi alınamadı — userId: {}", userId);
            return new AuditUserResponse(userId, null);
        }
    }

    private String getCurrentToken() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return null;
        return attributes.getRequest().getHeader("Authorization");
    }

    private Product setUpdatedBy(Product product) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getName())) {
                return product.withUpdatedBy(Long.valueOf(auth.getName()));
            }
        } catch (Exception e) {
            log.warn("updatedBy set edilemedi: {}", e.getMessage());
        }
        return product;
    }
}