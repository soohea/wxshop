package com.soohea.wxshop.generate;

import com.soohea.wxshop.generate.ShoppingCart;
import com.soohea.wxshop.generate.ShoppingCartExample;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ShoppingCartMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOPPING_CART
     *
     * @mbg.generated Fri Jul 01 12:48:02 CST 2022
     */
    long countByExample(ShoppingCartExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOPPING_CART
     *
     * @mbg.generated Fri Jul 01 12:48:02 CST 2022
     */
    int deleteByExample(ShoppingCartExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOPPING_CART
     *
     * @mbg.generated Fri Jul 01 12:48:02 CST 2022
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOPPING_CART
     *
     * @mbg.generated Fri Jul 01 12:48:02 CST 2022
     */
    int insert(ShoppingCart record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOPPING_CART
     *
     * @mbg.generated Fri Jul 01 12:48:02 CST 2022
     */
    int insertSelective(ShoppingCart record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOPPING_CART
     *
     * @mbg.generated Fri Jul 01 12:48:02 CST 2022
     */
    List<ShoppingCart> selectByExample(ShoppingCartExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOPPING_CART
     *
     * @mbg.generated Fri Jul 01 12:48:02 CST 2022
     */
    ShoppingCart selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOPPING_CART
     *
     * @mbg.generated Fri Jul 01 12:48:02 CST 2022
     */
    int updateByExampleSelective(@Param("record") ShoppingCart record, @Param("example") ShoppingCartExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOPPING_CART
     *
     * @mbg.generated Fri Jul 01 12:48:02 CST 2022
     */
    int updateByExample(@Param("record") ShoppingCart record, @Param("example") ShoppingCartExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOPPING_CART
     *
     * @mbg.generated Fri Jul 01 12:48:02 CST 2022
     */
    int updateByPrimaryKeySelective(ShoppingCart record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table SHOPPING_CART
     *
     * @mbg.generated Fri Jul 01 12:48:02 CST 2022
     */
    int updateByPrimaryKey(ShoppingCart record);
}