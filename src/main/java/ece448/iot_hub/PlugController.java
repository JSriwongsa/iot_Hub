
package ece448.iot_hub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.HashMap;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@RestController
@RequestMapping("/api/plugs")
public class PlugController {

    @Autowired
    public HubMqttController mqttController;
 
    @GetMapping("/{name}")
    public ResponseEntity<Plug> getPlug(@PathVariable("name") String name, @RequestParam(value= "action", required = false) String action) {

        HashMap<String, String> plugMqtt = mqttController.getPlug(name);
        Plug plug = new Plug(name, plugMqtt.get("state"), plugMqtt.get("power"));

        logger.info("Received API Request: /api/plugs/{}", name);
        
        if (action != null) {
            mqttController.publishAction(name, action);
            plug.setState(plugMqtt.get("state"));
            plug.setPower(plugMqtt.get("power"));
            return new ResponseEntity<>(plug, HttpStatus.OK);
        }

        return new ResponseEntity<>(plug, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Plug>> getAllPlugs() {
        List<Plug> plugs = mqttController.getPlugs();
        return new ResponseEntity<>(plugs, HttpStatus.OK);
    }
    private static final Logger logger = LoggerFactory.getLogger(PlugController.class);

}

