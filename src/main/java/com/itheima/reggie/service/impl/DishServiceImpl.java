package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.pojo.Dish;
import com.itheima.reggie.pojo.DishFlavor;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

//    @Override
//    public Page<DishDto> pageQuery(int page, int pageSize, String name) {
//        //构造分页构造器
//        Page<Dish> pageInfo = new Page<>(page, pageSize);
//        Page<DishDto> dtoPage = new Page<>();
//        //构造条件构造器
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        //添加排序条件
//        queryWrapper.orderByDesc(Dish::getUpdateTime);
//        queryWrapper.like(name != null, Dish::getName, name);
//        this.page(pageInfo, queryWrapper);
//
//        //对象拷贝 由于dish表中没有categoryName字段
//        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
//
//        List<Dish> records = pageInfo.getRecords();
//        List<DishDto> list = records.stream().map((item) -> {
//            DishDto dishDto = new DishDto();
//            BeanUtils.copyProperties(item, dishDto);
//            Long categoryId = item.getCategoryId();
//            Category category = categoryService.getById(categoryId);
//            String categoryName = category.getName();
//            dishDto.setCategoryName(categoryName);
//            return dishDto;
//        }).collect(Collectors.toList());
//        dtoPage.setRecords(list);
//        return dtoPage;
//    }

    /**
     * 新增菜品同时保存对应的口味数据
     *
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表中
        this.save(dishDto);
        //由于dishDto没有dishid这个字段，而dishflavor中有这个字段且需要进行操作的flavors集合中不含有dishId
        Long dishId = dishDto.getId();

        //菜品列表 遍历flavors集合将菜品id加入到该集合中
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());


        //保存菜品的口味到dishflavor表中
        dishFlavorService.saveBatch(dishDto.getFlavors());
    }

    /**
     * 根据id删除菜品并删除该菜品对应的口味
     *
     * @param ids
     */
    @Override
    @Transactional
    public void delDishAndDishFlavor(List<Long> ids) {

        //构造条件查询器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //先查询该菜品是否在售卖，如果是则抛出业务异常
        queryWrapper.in(ids != null && !ids.isEmpty(), Dish::getId, ids);
        List<Dish> list = this.list(queryWrapper);
        List<Long> notSellingDishIds = new ArrayList<>();
        for (Dish dish : list) {
            Integer status = dish.getStatus();
            if (status == 0) {
                //如果不是在售卖,则可以删除
                this.removeById(dish.getId());
                // 用于存储不在售卖的菜品的 id
                notSellingDishIds.add(dish.getId());
            } else {
                //此时应该回滚,因为可能前面的删除了，但是后面的是正在售卖
                throw new CustomException("删除菜品中有正在售卖菜品,无法全部删除");
            }
        }
        LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(DishFlavor::getDishId,notSellingDishIds);
        dishFlavorService.remove(queryWrapper1);
    }

    /**
     * 根据id查询菜品信息及口味
     *
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        Dish dish = this.getById(id);
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    @Transactional
    @Override
    public void updateDishAndFlavor(DishDto dishDto) {
        //修改dish表
        this.updateById(dishDto);
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        List<DishFlavor> flavors = dishDto.getFlavors();
        List<DishFlavor> flavorList = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavorList);
    }

    /**
     * 修改菜品状态(单个或批量)
     *
     * @param status
     * @param ids
     * @return
     */
    @Override
    public String updateStatus(Integer status, List<Long> ids) {
        // 创建 LambdaQueryWrapper 对象，用于构建查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        // 动态添加 IN 条件，只有当 ids 不为 null 且不为空时，才添加条件
        queryWrapper.in(ids != null && !ids.isEmpty(), Dish::getId, ids);

        // 根据查询条件获取符合条件的菜品列表
        List<Dish> list = this.list(queryWrapper);

        // 遍历菜品列表，修改每个菜品的售卖状态
        for (Dish dish : list) {
            // 检查菜品对象是否为 null（虽然理论上这里不会为 null）
            if (dish != null) {
                // 设置菜品的售卖状态
                dish.setStatus(status);
                // 更新菜品信息
                this.updateById(dish);
            }
        }
        return "售卖状态修改成功";
    }
}
