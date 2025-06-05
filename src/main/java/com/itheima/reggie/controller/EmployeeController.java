package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.pojo.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 登入功能
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        HttpSession session = request.getSession();
        return employeeService.login(employee, session);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        employeeService.logout(request.getSession());
        return R.success("退出成功");
    }

    /**
     * 员工分页条件查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page:{},pageSize:{},name={}", page, pageSize, name);
        Page<Employee> pageInfo = employeeService.pageQuery(page, pageSize, name);
        return R.success(pageInfo);
    }

    /**
     * 新增员工
     *
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Employee employee) {
        log.info("新增员工，员工信息：{}", employee.toString());
        employeeService.saveEmployee(employee);
        return R.success("新增用户成功");
    }

    /**
     * 根据id修改员工信息
     *
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update( @RequestBody Employee employee) {
//        log.info(employee.toString());
//
//        long id = Thread.currentThread().getId();
//        log.info("线程的id：{}",id);

//        HttpSession session = request.getSession();
        employeeService.update(employee);
        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工 数据回显
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        Employee emp = employeeService.getById(id);
        if (emp != null) {
            return R.success(emp);
        }
        return R.error("没有查询到员工信息");
    }

}
