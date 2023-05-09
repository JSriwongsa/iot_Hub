package ece448.iot_hub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class GroupsControllerTests {
    
    private GroupsModel mockModel;
    private GroupsController controller;

    @BeforeEach
    public void setup() {
        mockModel = mock(GroupsModel.class);
        controller = new GroupsController(mockModel);
    }

    @Test
    @DisplayName("Test GET /api/groups/{group} without action param")
    public void testGetGroupWithoutAction() {
        String groupName = "group1";
        HashMap<String, Object> groupData = new HashMap<>();
        groupData.put("name", groupName);
        ArrayList<HashMap<String, Object>> members = new ArrayList<>();
        HashMap<String, Object> member1 = new HashMap<>();
        member1.put("name", "plug1");
        member1.put("state", "on");
        member1.put("power", "100.000");
        HashMap<String, Object> member2 = new HashMap<>();
        member2.put("name", "plug2");
        member2.put("state", "off");
        member2.put("power", "0.000");
        members.add(member1);
        members.add(member2);
        groupData.put("members", members);

        when(mockModel.getGroup(groupName)).thenReturn(groupData);

        ResponseEntity<Object> response = controller.getGroup(groupName, null);

        verify(mockModel).getGroup(groupName);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(groupData, response.getBody());
    }

    @Test
    @DisplayName("Test GET /api/groups/{group} with action param")
    public void testGetGroupWithAction() {
        String groupName = "group1";
        String action = "on";

        when(mockModel.getGroup(groupName)).thenReturn(new HashMap<>());

        ResponseEntity<Object> response = controller.getGroup(groupName, action);

        verify(mockModel).publishAction(groupName, action);
        verify(mockModel).getGroup(groupName);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new HashMap<>(), response.getBody());
    }

    @Test
    @DisplayName("Test GET /api/groups")
    public void testGetGroups() {

        String groupName1 = "group1";
        HashMap<String, Object> groupData1 = new HashMap<>();
        ArrayList<HashMap<String, Object>> members1 = new ArrayList<>();
        HashMap<String, Object> member1 = new HashMap<>();
        member1.put("name", "plug1");
        member1.put("state", "on");
        member1.put("power", "100.000");
        HashMap<String, Object> member2 = new HashMap<>();
        member2.put("name", "plug2");
        member2.put("state", "off");
        member2.put("power", "0.000");
        members1.add(member1);
        members1.add(member2);
        groupData1.put("members", members1);

        String groupName2 = "group2";
        HashMap<String, Object> groupData2 = new HashMap<>();
        ArrayList<HashMap<String, Object>> members2 = new ArrayList<>();
        HashMap<String, Object> member3 = new HashMap<>();
        member3.put("name", "plug3");
        member3.put("state", "on");
        member3.put("power", "100.000");
        HashMap<String, Object> member4 = new HashMap<>();
        member4.put("name", "plug4");
        member4.put("state", "off");
        member4.put("power", "0.000");
        members2.add(member3);
        members2.add(member4);
        groupData2.put("members", members2);

        mockModel.createGroup(groupName1, members1.stream()
            .map(member -> member.get("name").toString())
            .collect(Collectors.toList()));
        mockModel.createGroup(groupName2, members2.stream()
            .map(member -> member.get("name").toString())
            .collect(Collectors.toList()));
    
        ResponseEntity<Object> response = controller.getGroups();
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Test DELETE /api/groups/{group}")
    public void testDeleteGroup() {
        GroupsModel groupsModel = new GroupsModel();
        String groupName = "group1";
        ArrayList<String> members = new ArrayList<String>();
        members.add("plug1");
        members.add("plug2");
        groupsModel.createGroup(groupName, members);
        Map<String, Group> groups = groupsModel.groups;
        assertTrue(groups.containsKey(groupName));
        groupsModel.removeGroup(groupName);
        assertFalse(groups.containsKey(groupName));
    }

}
