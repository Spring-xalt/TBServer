Apipost测试eg：
 1.LoginFilter：
    GET http://localhost:8080/taobao/product/all 正常
    GET http://localhost:8080/taobao/cart/list 拦截("code":401,"msg":"请先登录")

 2.RoleFilter：
    消费者登录：
    POST http://localhost:8080/taobao/auth/consumer/login Body：{"username":"jimi","password":"123456"}
    GET http://localhost:8080/taobao/cart/list 正常
    GET http://localhost:8080/taobao/merchant/my-products 权限不足
    GET http://localhost:8080/taobao/admin/statistics 权限不足