package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.pojo.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
//    Page<DishDto> pageQuery(int page, int pageSize,String name);


    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表 dish dishflavor
    void saveWithFlavor(DishDto dishDto);

    void delDishAndDishFlavor(List<Long> ids);

    DishDto getByIdWithFlavor(Long id);

    void  updateDishAndFlavor(DishDto dishDto);


    String updateStatus(Integer status, List<Long> ids);
}
