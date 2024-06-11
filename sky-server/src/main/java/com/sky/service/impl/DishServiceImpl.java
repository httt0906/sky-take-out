package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
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
    @Autowired
    private SetmealDishMapper setmealDishMapper;


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

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        // 通过这种方式,开发者无需在代码中手动拼接分页相关的 SQL 语句
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        PageResult pageResult = new PageResult(page.getTotal(),page.getResult());
        return pageResult;
    }


    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {

        // 判断菜品是否能够删除
        // -是否有在售状态的菜品
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // -菜品是否被套餐关联
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            // 该菜品被套餐关联 不可删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 删除菜品
        for (Long id : ids) {
            // 删除菜品
            dishMapper.deleteById(id);
            // 删除被菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(id);
        }


    }
}
