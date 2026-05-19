演示前需要开启的服务
 MySQL 数据库
   确保 MySQL 服务正在运行。

 Spring Boot 项目
   在 IDEA 中启动你的 MysqlConnectedApplication（端口 8080）。

 NATApp 内网穿透（支付宝回调必须）
   打开命令行，进入 D:\computer 目录，执行：
    D:
    cd computer
    .\natapp.exe -authtoken=8c8cec195b2882b1
    http://pa26a287.natappfree.cc(动态域名) -> http://127.0.0.1:8080
    注意：NATApp 必须一直保持运行，不能关窗口。

演示时需要的账号信息(内网支付这些)：
    沙箱支付宝APP	dggleh9671@sandbox.com/11111
    authtoken	8c8cec195b2882b1 
    当前域名	v59d3226.natappfree.cc
    notify-url	http://q2c8a8d6.natappfree.cc/taobao/alipay/notify
    (注意：notify-url可能经常变化，每次启动natapp需要更新yml)
    启动命令	.\natapp.exe -authtoken=8c8cec195b2882b1