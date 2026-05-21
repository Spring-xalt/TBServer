演示前需要开启的服务
1. MySQL 数据库
   确保 MySQL 服务正在运行。如果没启，可以用命令行或服务面板开启。

2. Spring Boot 项目
   在 IDEA 中启动你的 TBServerApplication（端口 8080）。

3. NATApp 内网穿透（支付宝回调必须）
   打开命令行，进入 D:\computer 目录，执行：
    D:
    cd computer
    .\natapp.exe -authtoken=8c8cec195b2882b1
    http://uc6a3ed2.natappfree.cc -> http://127.0.0.1:8080
    注意：NATApp 必须一直保持运行，不能关窗口。

演示时需要的账号信息(内网支付这些)：
    沙箱支付宝APP	dggleh9671@sandbox.com/11111
    authtoken	8c8cec195b2882b1 
    当前域名	pa26a287.natappfree.cc 
    notify-url	http://uc6a3ed2.natappfree.cc/taobao/alipay/notify
    启动命令	.\natapp.exe -authtoken=8c8cec195b2882b1