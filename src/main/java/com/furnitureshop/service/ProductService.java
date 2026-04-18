package com.furnitureshop.service;

import com.furnitureshop.model.Product;
import com.furnitureshop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> getAllActiveProducts() {
        return productRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Product> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return getAllActiveProducts();
        }
        return productRepository.searchActive(query.trim());
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Product addStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive");
        }
        product.setStockQuantity(product.getStockQuantity() + quantity);
        return productRepository.save(product);
    }

    public void deductStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        if (product.getStockQuantity() < quantity) {
            throw new IllegalStateException(
                    "Insufficient stock for '" + product.getName() + "'. Available: " + product.getStockQuantity());
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
    }

    public void deactivate(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStock(5);
    }

    @Transactional(readOnly = true)
    public long countOutOfStock() {
        return productRepository.countOutOfStock();
    }

    @Transactional(readOnly = true)
    public long countLowStock() {
        return productRepository.countLowStock();
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return productRepository.findByActiveTrue().size();
    }
}
