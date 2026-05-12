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
public class RoleFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI();

        // 只拦截需要角色控制的路径
        if (!needsRoleCheck(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;

        // 消费者专属(做role校验)
        if (path.startsWith("/taobao/cart/") ||
                path.startsWith("/taobao/review/my") ||
                path.startsWith("/taobao/review/reviewable") ||
                path.startsWith("/taobao/review/submit") ||
                path.startsWith("/taobao/consumer/") ||
                path.startsWith("/taobao/refund/consumer/")) {
            if (!"consumer".equals(role)) {
                send403(response);
                return;
            }
        }

        // 商户专属
        if (path.startsWith("/taobao/merchant/") ||
                path.startsWith("/taobao/order/merchant/") ||
                path.startsWith("/taobao/review/merchant/") ||
                path.startsWith("/taobao/refund/merchant/")) {
            if (!"merchant".equals(role)) {
                send403(response);
                return;
            }
        }

        // 管理员专属
        if (path.startsWith("/taobao/admin/")) {
            if (!"admin".equals(role)) {
                send403(response);
                return;
            }
        }

        // 其他需要登录但角色不限制的路径（如 /chat, /product/update 等）直接放行
        chain.doFilter(request, response);
    }

    private boolean needsRoleCheck(String path) {
        return path.startsWith("/taobao/cart/") ||
                path.startsWith("/taobao/order/") ||
                path.startsWith("/taobao/review/") ||
                path.startsWith("/taobao/refund/") ||
                path.startsWith("/taobao/consumer/") ||
                path.startsWith("/taobao/merchant/") ||
                path.startsWith("/taobao/admin/") ||
                path.startsWith("/taobao/chat/") ||
                path.startsWith("/taobao/product/update") ||
                path.startsWith("/taobao/product/delete") ||
                path.startsWith("/taobao/product/add");
    }

    private void send403(HttpServletResponse response) throws IOException {
        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"msg\":\"权限不足\"}");
    }
}
