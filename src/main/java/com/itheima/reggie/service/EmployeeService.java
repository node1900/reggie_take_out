package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.pojo.Employee;
import javax.servlet.http.HttpSession;

public interface EmployeeService extends IService<Employee> {
    /**
     * 登入
     * @param session
     * @return
     */
    public R<Employee> login(Employee employee, HttpSession session);

    /**
     * 登出
     * @param session
     * @return
     */
    public void logout(HttpSession session);

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    public Page<Employee> pageQuery(int page, int pageSize, String name);

    public void saveEmployee(Employee employee);

    public void update( Employee employee);
}
