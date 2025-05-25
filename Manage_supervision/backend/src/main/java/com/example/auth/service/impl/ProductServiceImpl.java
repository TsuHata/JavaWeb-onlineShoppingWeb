package com.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.auth.model.dto.PageResponse;
import com.example.auth.model.dto.ProductAuditDTO;
import com.example.auth.model.dto.ProductDTO;
import com.example.auth.model.entity.Category;
import com.example.auth.model.entity.Product;
import com.example.auth.model.entity.User;
import com.example.auth.mapper.CategoryMapper;
import com.example.auth.mapper.ProductMapper;
import com.example.auth.mapper.UserMapper;
import com.example.auth.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Value("${file.upload.path:/uploads}")
    private String uploadPath;
    
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, Long merchantId) {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setImageUrl(productDTO.getImageUrl());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());
        product.setCategoryId(productDTO.getCategoryId());
        product.setMerchantId(merchantId);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        product.setStatus("pending"); // 初始状态为待审核
        
        productMapper.insert(product);
        
        return convertToProductDTO(product);
    }
    
    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO, Long merchantId) {
        Product product = productMapper.selectById(id);
        
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("商品不存在或无权修改");
        }
        
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setImageUrl(productDTO.getImageUrl());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());
        product.setCategoryId(productDTO.getCategoryId());
        product.setUpdateTime(LocalDateTime.now());
        product.setStatus("pending"); // 修改后重新审核
        
        productMapper.updateById(product);
        
        return convertToProductDTO(product);
    }
    
    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return null;
        }
        
        return convertToProductDTO(product);
    }
    
    @Override
    public PageResponse<ProductDTO> getMerchantProducts(Long merchantId, int page, int size, String status) {
        Page<Product> pageParam = new Page<>(page, size);
        Page<Product> productPage;
        
        if (status != null && !status.isEmpty()) {
            productPage = productMapper.findByMerchantIdAndStatus(pageParam, merchantId, status);
        } else {
            productPage = productMapper.findByMerchantId(pageParam, merchantId);
        }
        
        List<ProductDTO> productDTOs = convertToProductDTOList(productPage.getRecords());
        
        return new PageResponse<>(productDTOs, productPage.getTotal(), page, size);
    }
    
    @Override
    public PageResponse<ProductDTO> getAllProducts(int page, int size, String status) {
        Page<Product> pageParam = new Page<>(page, size);
        Page<Product> productPage;
        
        if (status != null && !status.isEmpty()) {
            productPage = productMapper.findByStatus(pageParam, status);
        } else {
            productPage = productMapper.findAllProducts(pageParam);
        }
        
        List<ProductDTO> productDTOs = convertToProductDTOList(productPage.getRecords());
        
        return new PageResponse<>(productDTOs, productPage.getTotal(), page, size);
    }
    
    @Override
    public PageResponse<ProductDTO> getProductsWithFilters(
            int page, int size, String status, Long merchantId, Long categoryId,
            BigDecimal minPrice, BigDecimal maxPrice, Integer minStock, Integer maxStock,
            String startDate, String endDate, String keyword, String sortBy, String sortDirection) {
        
        // 创建查询条件
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        
        // 根据状态筛选
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }
        
        // 根据商家ID筛选
        if (merchantId != null) {
            queryWrapper.eq("merchant_id", merchantId);
        }
        
        // 根据分类ID筛选
        if (categoryId != null) {
            // 存储所有需要查询的分类ID
            List<Long> categoryIds = new ArrayList<>();
            categoryIds.add(categoryId);
            
            // 查找当前分类的所有子分类
            findChildCategories(categoryId, categoryIds);
            
            // 使用 IN 条件查询指定分类及其子分类下的所有商品
            queryWrapper.in("category_id", categoryIds);
        }
        
        // 根据价格范围筛选
        if (minPrice != null) {
            queryWrapper.ge("price", minPrice);
        }
        if (maxPrice != null) {
            queryWrapper.le("price", maxPrice);
        }
        
        // 根据库存范围筛选
        if (minStock != null) {
            queryWrapper.ge("stock", minStock);
        }
        if (maxStock != null) {
            queryWrapper.le("stock", maxStock);
        }
        
        // 根据日期范围筛选
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        
        if (StringUtils.hasText(startDate)) {
            try {
                startDateTime = LocalDateTime.parse(startDate + " 00:00:00", formatter);
                queryWrapper.ge("create_time", startDateTime);
            } catch (DateTimeParseException e) {
                try {
                    // 尝试只用日期格式解析
                    startDateTime = LocalDateTime.parse(startDate + " 00:00:00", 
                                     DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    queryWrapper.ge("create_time", startDateTime);
                } catch (DateTimeParseException ex) {
                    // 解析失败，忽略此筛选条件
                    System.err.println("无法解析开始日期: " + startDate);
                }
            }
        }
        
        if (StringUtils.hasText(endDate)) {
            try {
                endDateTime = LocalDateTime.parse(endDate + " 23:59:59", formatter);
                queryWrapper.le("create_time", endDateTime);
            } catch (DateTimeParseException e) {
                try {
                    // 尝试只用日期格式解析
                    endDateTime = LocalDateTime.parse(endDate + " 23:59:59", 
                                   DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    queryWrapper.le("create_time", endDateTime);
                } catch (DateTimeParseException ex) {
                    // 解析失败，忽略此筛选条件
                    System.err.println("无法解析结束日期: " + endDate);
                }
            }
        }
        
        // 关键字搜索（商品名称、描述）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> 
                wrapper.like("name", keyword)
                       .or()
                       .like("description", keyword)
            );
        }
        
        // 排序
        if (StringUtils.hasText(sortBy)) {
            String dbField = getDbFieldName(sortBy);
            if ("asc".equalsIgnoreCase(sortDirection)) {
                queryWrapper.orderByAsc(dbField);
            } else {
                queryWrapper.orderByDesc(dbField);
            }
        } else {
            // 默认按id降序排序
            queryWrapper.orderByDesc("id");
        }
        
        // 执行分页查询
        Page<Product> pageParam = new Page<>(page, size);
        Page<Product> productPage = productMapper.selectPage(pageParam, queryWrapper);
        
        // 转换为DTO列表
        List<ProductDTO> productDTOs = convertToProductDTOList(productPage.getRecords());
        
        return new PageResponse<>(productDTOs, productPage.getTotal(), page, size);
    }
    
    // 将前端排序字段名转换为数据库字段名
    private String getDbFieldName(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "name":
                return "name";
            case "price":
                return "price";
            case "stock":
                return "stock";
            case "createtime":
            case "createdate":
            case "created":
                return "create_time";
            case "updatetime":
            case "updatedate":
            case "updated":
                return "update_time";
            case "merchantid":
            case "merchant":
                return "merchant_id";
            case "categoryid":
            case "category":
                return "category_id";
            case "status":
                return "status";
            case "id":
            default:
                return "id";
        }
    }
    
    @Override
    @Transactional
    public ProductDTO auditProduct(ProductAuditDTO auditDTO, Long adminId) {
        Product product = productMapper.selectById(auditDTO.getProductId());
        
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        product.setStatus(auditDTO.getStatus());
        product.setAuditComment(auditDTO.getAuditComment());
        product.setAuditTime(LocalDateTime.now());
        product.setAuditUserId(adminId);
        
        productMapper.updateById(product);
        
        return convertToProductDTO(product);
    }
    
    @Override
    public PageResponse<ProductDTO> getProductsByCategory(Long categoryId, int page, int size) {
        Page<Product> pageParam = new Page<>(page, size);
        
        // 创建查询条件
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "approved"); // 只查询已审核通过的商品
        
        // 存储所有需要查询的分类ID
        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(categoryId);
        
        // 查找当前分类的所有子分类
        findChildCategories(categoryId, categoryIds);
        
        // 使用 IN 条件查询指定分类及其子分类下的所有商品
        queryWrapper.in("category_id", categoryIds);
        queryWrapper.orderByDesc("create_time"); // 按创建时间倒序排序
        
        Page<Product> productPage = productMapper.selectPage(pageParam, queryWrapper);
        
        List<ProductDTO> productDTOs = convertToProductDTOList(productPage.getRecords());
        
        return new PageResponse<>(productDTOs, productPage.getTotal(), page, size);
    }
    
    /**
     * 递归查找所有子分类ID
     */
    private void findChildCategories(Long parentId, List<Long> categoryIds) {
        List<Category> children = categoryMapper.findByParentId(parentId);
        if (children != null && !children.isEmpty()) {
            for (Category child : children) {
                categoryIds.add(child.getId());
                findChildCategories(child.getId(), categoryIds);
            }
        }
    }
    
    @Override
    public PageResponse<ProductDTO> searchProducts(String keyword, int page, int size) {
        Page<Product> pageParam = new Page<>(page, size);
        Page<Product> productPage = productMapper.searchProducts(pageParam, keyword);
        
        List<ProductDTO> productDTOs = convertToProductDTOList(productPage.getRecords());
        
        return new PageResponse<>(productDTOs, productPage.getTotal(), page, size);
    }
    
    @Override
    @Transactional
    public void deleteProduct(Long id, Long merchantId) {
        Product product = productMapper.selectById(id);
        
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("商品不存在或无权删除");
        }
        
        productMapper.deleteById(id);
    }
    
    @Override
    public String uploadProductImage(byte[] imageData, String filename) {
        try {
            // 确保上传目录存在
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 获取绝对路径便于调试
            String absolutePath = uploadDir.getAbsolutePath();
            System.out.println("上传目录绝对路径: " + absolutePath);
            
            // 生成唯一的文件名
            String extension = "";
            if (filename != null && filename.contains(".")) {
                extension = filename.substring(filename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            
            // 创建完整路径
            String filePath = absolutePath + File.separator + uniqueFilename;
            System.out.println("文件保存路径: " + filePath);
            
            // 写入文件
            FileOutputStream outputStream = new FileOutputStream(filePath);
            outputStream.write(imageData);
            outputStream.close();
            
            // 返回可访问的URL路径
            String imageUrl = "/uploads/" + uniqueFilename;
            System.out.println("返回的图片URL: " + imageUrl);
            return imageUrl;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public ProductDTO adminUpdateProduct(Long id, ProductDTO productDTO, Long adminId) {
        Product product = productMapper.selectById(id);
        
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        // 管理员更新商品无需修改状态为pending，保持原状态
        // 同时保留原商家ID
        Long originalMerchantId = product.getMerchantId();
        
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setImageUrl(productDTO.getImageUrl());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());
        product.setCategoryId(productDTO.getCategoryId());
        product.setUpdateTime(LocalDateTime.now());
        
        // 如果前端传递了新的商家ID且与原ID不同，则更新商家ID
        if (productDTO.getMerchantId() != null && !productDTO.getMerchantId().equals(originalMerchantId)) {
            // 验证新商家ID是否存在且为MERCHANT角色
            User merchant = userMapper.selectById(productDTO.getMerchantId());
            if (merchant == null) {
                throw new RuntimeException("指定的商家不存在");
            }
            
            // 检查是否为商家角色的验证逻辑应根据实际系统设计来实现
            // 这里简单示例，实际可能需要更复杂的验证
            boolean isMerchant = userMapper.hasRole(productDTO.getMerchantId(), "MERCHANT");
            if (!isMerchant) {
                throw new RuntimeException("指定的用户不是商家角色");
            }
            
            product.setMerchantId(productDTO.getMerchantId());
        }
        
        productMapper.updateById(product);
        
        return convertToProductDTO(product);
    }
    
    @Override
    @Transactional
    public void adminDeleteProduct(Long id, Long adminId) {
        Product product = productMapper.selectById(id);
        
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        // 管理员删除商品直接执行，无需验证商家ID
        productMapper.deleteById(id);
    }
    
    private ProductDTO convertToProductDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setCategoryId(product.getCategoryId());
        dto.setMerchantId(product.getMerchantId());
        dto.setStatus(product.getStatus());
        dto.setAuditComment(product.getAuditComment());
        
        // 格式化时间
        if (product.getCreateTime() != null) {
            dto.setCreateTime(product.getCreateTime().format(formatter));
        }
        
        if (product.getUpdateTime() != null) {
            dto.setUpdateTime(product.getUpdateTime().format(formatter));
        }
        
        if (product.getAuditTime() != null) {
            dto.setAuditTime(product.getAuditTime().format(formatter));
        }
        
        // 获取分类名称
        if (product.getCategoryId() != null) {
            Category category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) {
                dto.setCategoryName(category.getName());
            }
        }
        
        // 获取商家名称
        if (product.getMerchantId() != null) {
            User merchant = userMapper.selectById(product.getMerchantId());
            if (merchant != null) {
                dto.setMerchantName(merchant.getUsername());
            }
        }
        
        // 获取审核人员名称
        if (product.getAuditUserId() != null) {
            User admin = userMapper.selectById(product.getAuditUserId());
            if (admin != null) {
                dto.setAuditUserName(admin.getUsername());
            }
        }
        
        return dto;
    }
    
    private List<ProductDTO> convertToProductDTOList(List<Product> products) {
        List<ProductDTO> dtos = new ArrayList<>();
        for (Product product : products) {
            dtos.add(convertToProductDTO(product));
        }
        return dtos;
    }
} 