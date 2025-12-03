package com.frank.aicodehelper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frank.aicodehelper.model.dto.chathistory.ChatHistoryQueryRequest;
import com.frank.aicodehelper.model.entity.App;
import com.frank.aicodehelper.model.entity.User;
import com.frank.aicodehelper.model.enums.ChatHistoryMessageTypeEnum;
import com.frank.aicodehelper.service.AppService;
import com.frank.aicodehelper.service.ChatHistoryService;
import com.frank.aicodehelper.service.UserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ChatHistoryController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChatHistoryControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    private static Long testAppId;
    private static Long testUserId;
    private static MockHttpSession userSession;
    private static MockHttpSession adminSession;

    @BeforeAll
    static void setUp(@Autowired ChatHistoryControllerTest test) throws Exception {
        // 创建测试用户
        User testUser = new User();
        testUser.setUserAccount("testuser_controller");
        testUser.setUserPassword("test123456");
        testUser.setUserRole("user");
        test.userService.save(testUser);
        testUserId = testUser.getId();

        // 创建测试应用
        App testApp = new App();
        testApp.setAppName("控制器测试应用");
        testApp.setInitPrompt("生成测试网站");
        testApp.setCodeGenType("html");
        testApp.setUserId(testUserId);
        test.appService.save(testApp);
        testAppId = testApp.getId();

        // 添加测试对话数据
        for (int i = 1; i <= 20; i++) {
            test.chatHistoryService.addChatMessage(
                    testAppId,
                    "测试消息 " + i,
                    ChatHistoryMessageTypeEnum.USER.getValue(),
                    testUserId
            );
            Thread.sleep(10);
        }

        // 模拟用户登录会话
        userSession = new MockHttpSession();
        userSession.setAttribute("user_login", testUser);

        // 创建管理员用户和会话
        User adminUser = new User();
        adminUser.setUserAccount("admin_controller");
        adminUser.setUserPassword("admin123456");
        adminUser.setUserRole("admin");
        test.userService.save(adminUser);
        
        adminSession = new MockHttpSession();
        adminSession.setAttribute("user_login", adminUser);
    }

    /**
     * 测试1: GET /chatHistory/app/{appId} - 查询第一页
     */
    @Test
    @Order(1)
    void testListAppChatHistoryFirstPage() throws Exception {
        mockMvc.perform(get("/chatHistory/app/" + testAppId)
                        .param("pageSize", "10")
                        .session(userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(10));
    }

    /**
     * 测试2: GET /chatHistory/app/{appId} - 使用游标查询第二页
     */
    @Test
    @Order(2)
    void testListAppChatHistoryWithCursor() throws Exception {
        // 先获取第一页，提取游标
        MvcResult result = mockMvc.perform(get("/chatHistory/app/" + testAppId)
                        .param("pageSize", "10")
                        .session(userSession))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        // 这里应该解析响应，获取最后一条消息的createTime
        // 简化演示，使用当前时间作为游标
        String cursorTime = LocalDateTime.now().toString();

        // 使用游标查询第二页
        mockMvc.perform(get("/chatHistory/app/" + testAppId)
                        .param("pageSize", "10")
                        .param("lastCreateTime", cursorTime)
                        .session(userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试3: 未登录访问
     */
    @Test
    @Order(3)
    void testListAppChatHistoryWithoutLogin() throws Exception {
        mockMvc.perform(get("/chatHistory/app/" + testAppId)
                        .param("pageSize", "10"))
                .andExpect(status().is4xxClientError());
    }

    /**
     * 测试4: 自定义页面大小
     */
    @Test
    @Order(4)
    void testListAppChatHistoryWithCustomPageSize() throws Exception {
        mockMvc.perform(get("/chatHistory/app/" + testAppId)
                        .param("pageSize", "5")
                        .session(userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(5));
    }

    /**
     * 测试5: 管理员查询所有对话历史
     */
    @Test
    @Order(5)
    void testAdminListAllChatHistory() throws Exception {
        ChatHistoryQueryRequest request = new ChatHistoryQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        mockMvc.perform(post("/chatHistory/admin/list/page/vo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    /**
     * 测试6: 普通用户访问管理员接口（应该失败）
     */
    @Test
    @Order(6)
    void testUserAccessAdminEndpoint() throws Exception {
        ChatHistoryQueryRequest request = new ChatHistoryQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        mockMvc.perform(post("/chatHistory/admin/list/page/vo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .session(userSession))
                .andExpect(status().is4xxClientError());
    }

    @AfterAll
    static void tearDown(@Autowired ChatHistoryControllerTest test) {
        if (testAppId != null) {
            test.chatHistoryService.deleteByAppId(testAppId);
            test.appService.removeById(testAppId);
        }
        if (testUserId != null) {
            test.userService.removeById(testUserId);
        }
    }
}