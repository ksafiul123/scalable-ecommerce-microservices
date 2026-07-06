package product_service.service;

import product_service.dto.CategoryDTO;
import product_service.dto.ProductDTO;
import product_service.entity.Category;
import product_service.entity.Product;
import product_service.repository.CategoryRepository;
import product_service.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository  = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // ── Category operations ──────────────────────────────────────────

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream().map(this::toCategoryDTO).collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        return toCategoryDTO(category);
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Category already exists: " + dto.getName());
        }
        Category category = new Category(dto.getName(), dto.getDescription());
        categoryRepository.save(category);
        return toCategoryDTO(category);
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        if (dto.getName()        != null) category.setName(dto.getName());
        if (dto.getDescription() != null) category.setDescription(dto.getDescription());
        categoryRepository.save(category);
        return toCategoryDTO(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        categoryRepository.deleteById(id);
    }

    // ── Product operations ───────────────────────────────────────────

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream().map(this::toProductDTO).collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        return toProductDTO(product);
    }

    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream().map(this::toProductDTO).collect(Collectors.toList());
    }

    public List<ProductDTO> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream().map(this::toProductDTO).collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + dto.getCategoryId()));

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(category);
        product.setImageUrl(dto.getImageUrl());

        productRepository.save(product);
        return toProductDTO(product);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        if (dto.getName()        != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice()       != null) product.setPrice(dto.getPrice());
        if (dto.getStock()       != null) product.setStock(dto.getStock());
        if (dto.getImageUrl()    != null) product.setImageUrl(dto.getImageUrl());
        if (dto.getCategoryId()  != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found: " + dto.getCategoryId()));
            product.setCategory(category);
        }

        productRepository.save(product);
        return toProductDTO(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        productRepository.deleteById(id);
    }

    @Transactional
    public void decrementStock(Long productId, int qty) {
        int updated = productRepository.decrementStock(productId, qty);
        if (updated == 0) {
            throw new RuntimeException("Insufficient stock for product: " + productId);
        }
    }

    @Transactional
    public void incrementStock(Long productId, int qty) {
        productRepository.incrementStock(productId, qty);
    }

    // ── Mappers ──────────────────────────────────────────────────────

    private ProductDTO toProductDTO(Product p) {
        return new ProductDTO(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getStock(),
                p.getCategory().getId(),
                p.getCategory().getName(),
                p.getImageUrl()
        );
    }

    private CategoryDTO toCategoryDTO(Category c) {
        return new CategoryDTO(c.getId(), c.getName(), c.getDescription());
    }
}
