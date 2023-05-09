package ece448.iot_hub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Collections;
import lombok.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class GroupsModel {

    public Map<String, Group> groups;

    @Autowired
    public HubMqttController mqttController;

    public GroupsModel() {
        this.groups = new HashMap<>();
    }

    @Generated
    synchronized public void publishAction(String groupName, String action) {
        Group group = this.groups.get(groupName);
        if (group == null) {
            logger.info("publishAction() Null Group: {}", group);
        } else {
            List<String> groupMembers = group.getPlugNames();
            for (String plugName : groupMembers) {
                mqttController.publishAction(plugName, action);
            }
        }
    }

    synchronized public void createGroup(String groupName) {
        createGroup(groupName, new ArrayList<String>());
    }

    synchronized public void createGroup(String groupName, List<String> plugNames) {
        Group group = new Group(groupName, plugNames);
        group.setPlugNames(plugNames);
        this.groups.put(groupName, group);
    }

    synchronized public void removeGroup(String groupName) {
        groups.remove(groupName);
    }

    synchronized public HashMap<String, Object> getGroup(String groupName) {
        HashMap<String, Object> ret = new HashMap<>();
        ret.put("name", groupName);

        Group group = groups.get(groupName);

        if (group == null) {
            // this should create a temporary group so we can protect against null and return an empty list in our response for members
            List<String> emptyList = Collections.<String>emptyList();
            Group emptyGroup = new Group(groupName, emptyList);
            emptyGroup.setPlugNames(emptyList);
            List<String> emptyMembers = emptyGroup.getPlugNames();
            ret.put("members", emptyMembers);
            return ret;
        }

        List<String> plugNames = group.getPlugNames();
        List<Plug> members = new ArrayList<>();

        for (String plugName : plugNames) {
            HashMap<String, String> plugMqtt = mqttController.getPlug(plugName);
            String plugState = plugMqtt.get("state");
            String plugPower = plugMqtt.get("power");
            // hack to make /api/groups not show null for some plugs
            if (plugState == null || plugPower == null) {
                plugState = "off";
                plugPower = "0.000";
            }
            Plug plug = new Plug(plugName, plugState, plugPower);
            members.add(plug);
        }
        ret.put("members", members);
        return ret;
    }
        

    synchronized public List<String> getGroupsNames() {
        List<String> groupNames = new ArrayList<>();
        for (Group group : this.getAllGroups()) {
            groupNames.add(group.getName());

        }
        return groupNames;
    }
    synchronized public List<Group> getAllGroups() {
        return new ArrayList<>(groups.values());
    }
    private static final Logger logger = LoggerFactory.getLogger(GroupsModel.class);

}
