Bug ：@RequestParam 缺少 name 属性导致 500


商家登录后访问 /merchant/my-products?page=1&size=8，返回 HTTP 500。
    浏览器 Network 面板无有效响应体，服务端抛异常： java.lang.IllegalArgumentException:
    影响范围逐步扩大后发现不只是 my-products，项目中所有未显式写 name 的 @RequestParam 方法
    在非 IDE 编译下全部宕机，共 15 处，涉及 5 个 Controller。

分析:
Spring MVC 如何绑定参数
    当请求 GET /merchant/my-products?page=1&size=8 到达时：
        Spring 找到方法 MerchantController.myProducts(int page, int size)
        通过 Java 反射获取参数名 "page" 和 "size"将 URL 中的 page,size → 绑定到参数 page，size
    然后关键来了Java 编译后的 .class 文件默认不保留方法参数名。
        .java 里的 int page 编译后变成 int arg0，
        Spring 反射拿到的名字就是 arg0，自然无法与 URL 的 page 匹配。
    

   故只要是用 Maven 命令行编译后启动的包，所有未显式指明 name 的 @RequestParam 全部抛 IllegalArgumentException。
    注意有两类写法都会中招：
        // 不带括号的（容易被搜索遗漏）
        @RequestParam String keyword
        // 带括号但有其他属性唯独没 name 的
        @RequestParam(defaultValue = "1") int page
        @RequestParam(required = false) Integer status

解决方案
   给每个 @RequestParam 显式加上 name 或 value（两者等价），不依赖编译器保留参数名：
    // 修复前
    @RequestParam(defaultValue = "1") int page
    // 修复后
    @RequestParam(name = "page", defaultValue = "1") int page
   本项目实际修复清单：
    MerchantController  — page, size, merchantName, keyword
    ProductController   — page, size, keyword
    ChatController      — userId1, userId2, targetId
    RefundController    — orderId, status, refundId, action
    AlipayController    — outTradeNo


开发规范：@RequestParam 一律显式写 name，这是 Spring 官方推荐，不依赖任何编译器行为

