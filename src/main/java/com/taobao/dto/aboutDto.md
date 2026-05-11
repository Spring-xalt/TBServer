不是所有 DTO 都要继承实体，要分场景看待

场景一：需要继承的 — VO（视图对象）
    典型代表：ReviewVO
    ReviewVO 本质上是把 Review 的所有基础属性（评分、内容、时间等）带上，再额外补充几个名称字段（商品名、商户名、消费者名）。
 用继承的好处：
    不用在 VO 中重复定义 score、content、create_time 等字段
    可以直接用 BeanUtils.copyProperties(review, vo) 把实体中的基础属性一键拷贝过来
    结论：VO 和实体是“增强”关系，应该继承。


场景二：不需要继承的 — 独立 DTO（数据传输对象）
    典型代表：CartItem(session临时对象)、MerchantDto(包含List<Product>)、
    UserLoginDto(用username 和 password接受对象并做验证)、UserRegisterDto
    这些 DTO 不是实体的增强版本，而是独立的数据结构：
    结论：独立 DTO 和实体是“不同用途”的关系，不应该继承。