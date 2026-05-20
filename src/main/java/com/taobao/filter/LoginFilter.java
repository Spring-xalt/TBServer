package com.taobao.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/*
 *@auther:Jimi
 *@version:1.0
 *@description:
 */
public class LoginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI();

        // 可放行的请求（不需要登录校验的）
        // auth以下的登录/注册，游客浏览搜索主页，模拟支付的支付宝回调接口，其他静态资源
        if (path.startsWith("/taobao/auth/") ||
                path.equals("/taobao/product/all") ||
                path.equals("/taobao/product/search") ||
                path.matches("/taobao/product/\\d+/image") ||
                path.equals("/taobao/alipay/notify") ||
                path.matches(".*\\.(html|css|js|jpg|png|ico|woff|ttf)$")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            send401(response);
            return;
        }

        Object consumerId = session.getAttribute("consumerId");
        Object merchantId = session.getAttribute("merchantId");
        Object adminId = session.getAttribute("adminId");

        if (consumerId == null && merchantId == null && adminId == null) {
            send401(response);
            return;
        }

        chain.doFilter(request, response);
    }

    private void send401(HttpServletResponse response) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"请先登录\"}");
    }


}
