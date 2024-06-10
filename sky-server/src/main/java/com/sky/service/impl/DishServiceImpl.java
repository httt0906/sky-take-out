package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;


    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Transactional // 事务注解 保证方法是原子性质的
    // 同时在启动类添加注解 @EnableTransactionManagement 开启注解方式的事务管理
    public void saveWithFlavor(DishDTO dishDTO) {

        // 因为有口味数据 所以插入菜品表不用dishDTO 使用dish实体类
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 向菜品表插入一条数据
        dishMapper.insert(dish);

        // 获取insert语句生成的主键值
        // 因为这是多表 口味表没办法直接获得菜品的id
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        // 向口味表插入多条数据
        if (flavors != null && flavors.size() > 0) {
            // 对每一个口味都赋菜品id值
            flavors.forEach(dishFlavor -> {dishFlavor.setDishId(dishId);});
            dishFlavorMapper.insertBatch(flavors);
        }

    }
}
