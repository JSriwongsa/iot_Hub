package ece448.iot_hub;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class GroupsModelTests {

    @Mock
    HubMqttController mockMqttController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateGroup() {
        GroupsModel groupsModel = new GroupsModel();
        groupsModel.mqttController = mockMqttController;

        String groupName = "testGroup";
        List<String> plugNames = Arrays.asList("plug1", "plug2");

        groupsModel.createGroup(groupName, plugNames);
        Map<String, Group> groups = groupsModel.groups;
        assertTrue(groups.containsKey(groupName));

        Group group = groups.get(groupName);
        assertEquals(group.getName(), groupName);
        assertEquals(group.getPlugNames(), plugNames);
    }

    @Test
    public void testRemoveGroup() {
        GroupsModel groupsModel = new GroupsModel();
        groupsModel.mqttController = mockMqttController;

        String groupName = "testGroup";
        List<String> plugNames = Arrays.asList("plug1", "plug2");

        groupsModel.createGroup(groupName, plugNames);

        groupsModel.removeGroup(groupName);

        Map<String, Group> groups = groupsModel.groups;
        assertFalse(groups.containsKey(groupName));
    }

    @Test
    public void testGetGroup() {

        // create group, add plugNames to it.
        GroupsModel groupsModel = new GroupsModel();
        List<String> plugNames = new ArrayList<>(); 
        plugNames.add("plug1");
        plugNames.add("plug2");
        groupsModel.createGroup("group1", plugNames);

        // mock mqtt
        HubMqttController mockMqttController = mock(HubMqttController.class);
        groupsModel.mqttController = mockMqttController;

        HashMap<String, String> plug1 = new HashMap<>();
        plug1.put("name", "plug1");
        plug1.put("state", "on");
        plug1.put("power", "100");

        HashMap<String, String> plug2 = new HashMap<>();
        plug2.put("name", "plug2");
        plug2.put("state", "off");
        plug2.put("power", "0");
        when(mockMqttController.getPlug("plug1")).thenReturn(plug1);
        when(mockMqttController.getPlug("plug2")).thenReturn(plug2);

        HashMap<String, Object> res = groupsModel.getGroup("group1");
        assertEquals(res.get("name"), "group1");
        assertEquals(((List<HashMap<String, Object>>) res.get("members")).size(), 2);
    }

    @Test
    public void testGetGroupNonExistent() {
        GroupsModel groupsModel = new GroupsModel();
        groupsModel.mqttController = mockMqttController;

        String groupName = "testGroup";

        HashMap<String, Object> group = groupsModel.getGroup(groupName);
        assertEquals(group.get("name"), groupName);

        List<Map<String, String>> members = (List<Map<String, String>>) group.get("members");
        assertTrue(members.isEmpty());
    }

    @Test
    public void testGetGroupsNames() {
        GroupsModel groupsModel = new GroupsModel();
            groupsModel.mqttController = mockMqttController;

            String groupName1 = "testGroup1";
            String groupName2 = "testGroup2";
            String groupName3 = "testGroup3";

            groupsModel.createGroup(groupName1);
            groupsModel.createGroup(groupName2);
            groupsModel.createGroup(groupName3);

            List<String> groupNames = groupsModel.getGroupsNames();
            assertEquals(groupNames.size(), 3);
    }
    
}
