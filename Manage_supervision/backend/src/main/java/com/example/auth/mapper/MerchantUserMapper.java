package com.example.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.auth.model.entity.MerchantUserRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MerchantUserMapper extends BaseMapper<MerchantUserRelation> {
    
    /**
     * 根据商家ID查询关系
     */
    @Select("SELECT * FROM merchant_user_relations WHERE merchant_id = #{merchantId}")
    List<MerchantUserRelation> findByMerchantId(@Param("merchantId") Long merchantId);
    
    /**
     * 根据用户ID查询关系
     */
    @Select("SELECT * FROM merchant_user_relations WHERE user_id = #{userId}")
    List<MerchantUserRelation> findByUserId(@Param("userId") Long userId);
    
    /**
     * 检查商家和用户是否存在关系
     */
    @Select("SELECT COUNT(*) > 0 FROM merchant_user_relations " +
            "WHERE merchant_id = #{merchantId} AND user_id = #{userId}")
    boolean existsByMerchantIdAndUserId(@Param("merchantId") Long merchantId, @Param("userId") Long userId);
    
    /**
     * 根据商家ID和用户ID查找关系
     */
    @Select("SELECT * FROM merchant_user_relations " +
            "WHERE merchant_id = #{merchantId} AND user_id = #{userId} LIMIT 1")
    MerchantUserRelation findByMerchantIdAndUserId(@Param("merchantId") Long merchantId, @Param("userId") Long userId);
    
    /**
     * 查询特定商家的所有有效用户ID
     */
    @Select("SELECT user_id FROM merchant_user_relations " +
            "WHERE merchant_id = #{merchantId} AND status = 'active'")
    List<Long> findActiveUserIdsByMerchantId(@Param("merchantId") Long merchantId);
    
    /**
     * 查询特定用户的所有有效商家ID
     */
    @Select("SELECT merchant_id FROM merchant_user_relations " +
            "WHERE user_id = #{userId} AND status = 'active'")
    List<Long> findActiveMerchantIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询未分配商家的用户ID列表
     */
    @Select("SELECT u.id FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "WHERE ur.role_id = #{roleId} " +
            "AND u.id NOT IN (SELECT user_id FROM merchant_user_relations WHERE status = 'active')")
    List<Long> findUnassignedUserIdsByRoleId(@Param("roleId") Long roleId);
} 