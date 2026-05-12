Apipost测试eg：
 1.LoginFilter：
    GET http://localhost:8080/taobao/product/all 正常
    GET http://localhost:8080/taobao/cart/list 拦截("code":401,"msg":"请先登录")

 2.RoleFilter：
    消费者登录：
    POST http://localhost:8080/taobao/auth/consumer/login Body：{"username":"jimi","password":"123456"}  (id=6)
    GET http://localhost:8080/taobao/order/consumer/6 正常(查我买过的订单)
    GET http://localhost:8080/taobao/merchant/my-products 权限不足
    GET http://localhost:8080/taobao/admin/statistics 权限不足