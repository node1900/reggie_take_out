package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.pojo.DishFlavor;

import java.util.List;


public interface DishFlavorService extends IService<DishFlavor> {

    void delByDishIds(List<Long> ids);
}
