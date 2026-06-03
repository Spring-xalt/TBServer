package com.taobao.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiChatResponse {
    private String reply;
    private List<String> suggestions;
}
