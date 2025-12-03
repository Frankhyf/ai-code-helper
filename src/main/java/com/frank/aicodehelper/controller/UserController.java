package com.frank.aicodehelper.controller;

import cn.hutool.core.bean.BeanUtil;
import com.frank.aicodehelper.annotation.AuthCheck;
import com.frank.aicodehelper.common.BaseResponse;
import com.frank.aicodehelper.common.ResultUtils;
import com.frank.aicodehelper.constant.UserConstant;
import com.frank.aicodehelper.exception.ErrorCode;
import com.frank.aicodehelper.exception.ThrowUtils;
import com.frank.aicodehelper.manager.CosManager;
import com.frank.aicodehelper.model.dto.user.UserAddRequest;
import com.frank.aicodehelper.model.dto.user.UserLoginRequest;
import com.frank.aicodehelper.model.dto.user.UserQueryRequest;
import com.frank.aicodehelper.model.dto.user.UserRegisterRequest;
import com.frank.aicodehelper.model.dto.user.UserUpdateMyRequest;
import com.frank.aicodehelper.model.entity.User;
import com.frank.aicodehelper.model.vo.LoginUserVO;
import com.frank.aicodehelper.model.vo.UserVO;
import com.frank.aicodehelper.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * 用户 控制层。
 *
 * @author 1
 * @since 2025-10-28
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;
    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @param request          请求对象
     * @return 脱敏后的用户登录信息
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户注销
     *
     * @param request 请求对象
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }
    /**
     * 上传用户头像
     *
     * @param file    头像文件
     * @param request 请求
     * @return 头像URL
     */
    @PostMapping("/upload/avatar")
    public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                             HttpServletRequest request) {
        // 1. 参数校验
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 2. 文件类型校验（只允许图片）
        String originalFilename = file.getOriginalFilename();
        ThrowUtils.throwIf(originalFilename == null, ErrorCode.PARAMS_ERROR, "文件名不能为空");
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String[] allowedSuffixes = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
        boolean isValid = false;
        for (String allowedSuffix : allowedSuffixes) {
            if (suffix.equalsIgnoreCase(allowedSuffix)) {
                isValid = true;
                break;
            }
        }
        ThrowUtils.throwIf(!isValid, ErrorCode.PARAMS_ERROR, "只支持 jpg、jpeg、png、gif、webp 格式的图片");
        // 3. 文件大小校验（限制 5MB）
        long maxSize = 5 * 1024 * 1024; // 5MB
        ThrowUtils.throwIf(file.getSize() > maxSize, ErrorCode.PARAMS_ERROR, "文件大小不能超过 5MB");
        // 4. 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 5. 生成COS存储路径：avatar/{userId}/{uuid}.{suffix}
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String cosKey = String.format("avatar/%d/%s%s", loginUser.getId(), uuid, suffix);
        // 6. 上传到COS
        String avatarUrl = cosManager.uploadFile(cosKey, file);
        ThrowUtils.throwIf(avatarUrl == null, ErrorCode.OPERATION_ERROR, "头像上传失败");
        // 7. 自动更新用户头像字段
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserAvatar(avatarUrl);
        boolean updateResult = userService.updateById(user);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新用户头像失败");
        return ResultUtils.success(avatarUrl);
    }

    /**
     * 用户更新自己的信息（用户名、头像、简介）
     *
     * @param userUpdateMyRequest 更新请求
     * @param request             请求
     * @return 是否成功
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                              HttpServletRequest request) {
        // 1. 参数校验
        ThrowUtils.throwIf(userUpdateMyRequest == null, ErrorCode.PARAMS_ERROR);
        // 2. 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 3. 构造更新对象（只允许更新特定字段）
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserName(userUpdateMyRequest.getUserName());
        user.setUserAvatar(userUpdateMyRequest.getUserAvatar());
        user.setUserProfile(userUpdateMyRequest.getUserProfile());
        // 4. 执行更新
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据主键更新用户（仅管理员）
     *
     * @param user 用户
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> update(@RequestBody User user) {
        boolean result = userService.updateById(user);
        return ResultUtils.success(result);
    }

    // ==================== 管理员专用接口 ====================

    /**
     * 管理员删除用户
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/admin/delete/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@PathVariable Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "用户ID无效");
        // 检查用户是否存在
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        boolean result = userService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 管理员获取用户详情（脱敏）
     *
     * @param id 用户ID
     * @return 用户详情（脱敏后）
     */
    @GetMapping("/admin/get/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserVO> getUserById(@PathVariable Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "用户ID无效");
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        // 返回脱敏后的用户信息
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 管理员获取用户列表（脱敏）
     *
     * @return 用户列表（脱敏后）
     */
    @GetMapping("/admin/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<UserVO>> listUsers() {
        List<User> userList = userService.list();
        List<UserVO> userVOList = userService.getUserVOList(userList);
        return ResultUtils.success(userVOList);
    }

    /**
     * 管理员分页查询用户（脱敏）
     *
     * @param userQueryRequest 查询请求
     * @return 用户分页（脱敏后）
     */
    @PostMapping("/admin/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = userQueryRequest.getPageNum();
        long pageSize = userQueryRequest.getPageSize();
        // 限制每页最多 50 条，防止一次性查询过多数据
        ThrowUtils.throwIf(pageSize > 50, ErrorCode.PARAMS_ERROR, "每页最多查询 50 条");
        // 构建查询条件
        QueryWrapper queryWrapper = userService.getQueryWrapper(userQueryRequest);
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 转换为脱敏后的 VO
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

}
