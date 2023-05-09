package ece448.iot_hub;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import lombok.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class GroupsController {

    public GroupsModel groups;

    public GroupsController(GroupsModel groups) {
        this.groups = groups;
    }

    @GetMapping("/api/groups/{group}")
    public ResponseEntity<Object> getGroup(
        @PathVariable("group") String group,
        @RequestParam(value = "action", required = false) String action){
            logger.info("GroupsController: API Request received for /api/groups/{}", group);
            if (action == null){
                Object ret = groups.getGroup(group);
                return new ResponseEntity<>(ret, HttpStatus.OK);
            } else {
                logger.info("GroupsController: API Request received with action param: {}", action);
                groups.publishAction(group, action);
                Object ret = groups.getGroup(group);
                return new ResponseEntity<>(ret, HttpStatus.OK);
            }
    }

    @GetMapping("/api/groups")
    public ResponseEntity<Object> getGroups(){
        logger.info("GroupsController: API Request received for /api/groups");
        ArrayList<HashMap<String, Object>> ret = new ArrayList<HashMap<String, Object>>();
        for (String groupName : groups.getGroupsNames()){ ret.add(groups.getGroup(groupName)); };
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @Generated
    @PostMapping("/api/groups/{group}")
    public void createGroup(
        @PathVariable("group") String group,
        @RequestBody ArrayList<String> members) {
        logger.info("REST Create group " + group + ": [" + String.join(", " ,members) + "]");
        groups.createGroup(group, members);
    }

    @Generated
    @DeleteMapping("/api/groups/{group}")
    public void deleteGroup(@PathVariable("group") String group) {
        logger.info("REST Delete group " + group);
        groups.removeGroup(group);
    }

    private static final Logger logger = LoggerFactory.getLogger(GroupsController.class);

}
