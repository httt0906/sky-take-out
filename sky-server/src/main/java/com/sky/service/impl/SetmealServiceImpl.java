package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    SetmealMapper setmealMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;


    @Transactional
    public void saveWithDishes(SetmealDTO setmealDTO) {
        // 1. 生成相应的套餐对象 将其插入套餐表
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);

        Long setmealId = setmeal.getId();

        // 2. 对于套餐中有哪些菜品 将其插入套餐-菜品表
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {setmealDish.setSetmealId(setmealId);});
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 分页查询套餐数据
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {

        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        PageResult pageResult = new PageResult(page.getTotal(),page.getResult());
        return pageResult;
    }

    /**
     * 根据id集合批量删除套餐
     * @param setmealIds
     * @return
     */
    public void deleteByIds(List<Long> setmealIds) {
        log.info("得到要删除的ids:{}", setmealIds);
        // 1. 根据id找到相应的套餐并判断是否能够删除
        setmealIds.forEach(setmealId->{
            log.info("希望得到要删除的套餐");
            Setmeal setmeal = setmealMapper.getById(setmealId);
            log.info("得到要删除的套餐:{}", setmeal.getName());
            if(setmeal.getStatus() == StatusConstant.ENABLE){
                // 启售中套餐不能删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });
        log.info("批量删除套餐表中相应的数据");

        // 2. 批量删除套餐表中相应的数据
        // delete from setmeal where id in (1,2,3);
        setmealMapper.deleteBatch(setmealIds);
        log.info("批量删除套餐-菜品表中相应的数据");

        // 3. 批量删除套餐-菜品表中相应的数据
        // delete from setmeal_dish where setmeal_id in (1,2,3)
        setmealDishMapper.deleteBatch(setmealIds);
    }
}
