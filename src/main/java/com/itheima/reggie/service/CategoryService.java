package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.pojo.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {
    Page<Category> pageQuery(int page, int pageSize);

    void remove(Long id);

    List<Category> listByType(Category category);
}
