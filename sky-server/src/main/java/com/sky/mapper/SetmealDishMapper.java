package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询对应套餐id
     *
     * @param dishIds
     * @return
     */
    public List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 向套餐-菜品表中批量插入相关信息
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    void deleteBatch(List<Long> setmealIds);
}
