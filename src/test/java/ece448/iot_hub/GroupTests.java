package ece448.iot_hub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

public class GroupTests {
    private Group group;
    private String groupName;
    private List<String> plugNames;

    @BeforeEach
    void setUp() {
        groupName = "testGroup";
        plugNames = Arrays.asList("plug1", "plug2", "plug3");
        group = new Group(groupName, plugNames);
    }

    @Test
    void testGetName() {
        assertEquals(groupName, group.getName());
    }

    @Test
    void testGetPlugNames() {
        assertEquals(plugNames, group.getPlugNames());
    }

    @Test
    void testSetPlugNames() {
        List<String> newPlugNames = Arrays.asList("plug4", "plug5");
        group.setPlugNames(newPlugNames);
        assertEquals(newPlugNames, group.getPlugNames());
    }
    
}
