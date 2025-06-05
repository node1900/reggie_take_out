package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.R;
import com.itheima.reggie.mapper.EmployeeMapper;
import com.itheima.reggie.pojo.Employee;
import com.itheima.reggie.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employee
     * @param session  HTTP 会话，用于存储登录员工 ID
     * @return 登录结果，成功返回员工信息，失败返回错误信息
     */
    @Override
    public R<Employee> login(Employee employee, HttpSession session) {
        String username = employee.getUsername();
        String password = employee.getPassword();
        //        1、将页面提交的密码提交给password进行MD5加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());

//      2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, username);
        Employee emp = employeeMapper.selectOne(queryWrapper);

//        3、如果没有查询到则返回登录失败结果
        if (emp == null) {
            return R.error("登录失败");
        }

//        4、密码比对，如果不一致则返回登陆失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败");
        }

//        5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("该账号已禁用");
        }

//        6、登录成功，将员工id存入session并返回登录结果
        session.setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    /**
     * 登出
     *
     * @param session HTTP会话，用于移除登录员工信息
     */
    @Override
    public void logout(HttpSession session) {
        session.removeAttribute("employee");
    }

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @Override
    public Page<Employee> pageQuery(int page, int pageSize, String name) {
        //构造分页构造器
        Page pageInfo = new Page<>(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件  判断name值是否为空，不为空则添加模糊查询条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        this.page(pageInfo, queryWrapper);
        return pageInfo;
    }

    /**
     * 新增员工
     *
     * @param session
     * @param emp
     */
    @Override
    public void saveEmployee(Employee emp) {
        //设置初始密码并进行md5加密
        emp.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //设置添加时间和更新时间
//        emp.setCreateTime(LocalDateTime.now());
//        emp.setUpdateTime(LocalDateTime.now());
//        //获取当前登录的用户id
//        Long empId = (Long) session.getAttribute("employee");
//        //设置进行该操作的用户
//        emp.setCreateUser(empId);
//        emp.setUpdateUser(empId);

        //MP增加员工方法
        save(emp);
    }

    @Override
    public void update( Employee employee) {
//        Long empId = (Long) session.getAttribute("employee");
//        employee.setUpdateUser(empId);
//        employee.setUpdateTime(LocalDateTime.now());
        updateById(employee);
    }


}
