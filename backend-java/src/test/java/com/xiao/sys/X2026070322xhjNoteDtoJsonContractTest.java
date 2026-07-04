package com.xiao.sys;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiao.sys.dto.NoteDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 约束备忘录接口的 JSON 字段映射，避免前后端字段名再次跑偏。
 */
class X2026070322xhjNoteDtoJsonContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeSnakeCaseFields() throws Exception {
        String json = """
                {
                  "id": 12,
                  "user_id": 3,
                  "org_id": 8,
                  "title": "周会纪要",
                  "content": "跟进待办",
                  "tags": "[\\"工作\\",\\"重点\\"]",
                  "note_type": "work",
                  "create_time": "2026-07-03 10:00:00",
                  "update_time": "2026-07-03 11:00:00"
                }
                """;

        NoteDTO dto = objectMapper.readValue(json, NoteDTO.class);

        assertEquals(12, dto.getId());
        assertEquals(3, dto.getUserId());
        assertEquals(8, dto.getOrgId());
        assertEquals("work", dto.getNoteType());
        assertEquals("2026-07-03 10:00:00", dto.getCreateTime());
        assertEquals("2026-07-03 11:00:00", dto.getUpdateTime());
    }
}
