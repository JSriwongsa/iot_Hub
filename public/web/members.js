/**
 * A model for managing members in groups.
 */
function create_members_model(groups) {
	// create the data structure
	var all_members = new Set(); // all unique member names
	var group_names = [];
	var group_states = new Map();
	var group_members = new Map(); // group_name to set of group members
	for (var group of groups) {
		group_names.push(group.name);
		var members = new Set(group.members);
		members.forEach(member => all_members.add(member.name));
		members.forEach(member => group_states.set(group.name, member.state));
		//group_members.set(group.name, members);
		group_members.set(group.name, all_members);
		
		console.info("create_members_model: group_members:" + group_members);
	}
	var member_names = Array.from(all_members);
	var group_state = Array.from(group_states);``
	group_names.sort();
	member_names.sort();

	// create the object
	var that = {}
	that.get_group_names = () => group_names;
	that.get_member_names = () => member_names;
	that.get_group_state = group_name => group_states.get(group_name);
	that.is_member_in_group = (member_name, group_name) =>
		!group_members.has(group_name)? false:
			group_members.get(group_name).has(member_name);
	that.get_group_members = group_name => group_members.get(group_name);

	console.info("Members Model",
		groups, group_names, member_names, group_members);
	console.info("Group state: " + JSON.stringify(group_state));

	return that;
}

/**
 * The Members controller holds the state of groups.
 * It creates its view in render().
 */
class Members extends React.Component {

	constructor(props) {
		super(props);
		console.info("Members constructor()");
		this.state = {
			members: create_members_model([]),
			inputName: "",
			inputMembers: "",
		};
	}

	componentDidMount() {
		console.info("Members componentDidMount()");
		this.getGroups();
		window.setInterval(() => this.getGroups(), 2000);
		//setInterval(this.getGroups, 1000);
	}

	render() {
		//console.info("Members render()");
		return (<MembersTable members={this.state.members}
			inputName={this.state.inputName} inputMembers={this.state.inputMembers}
			onMemberChange={this.onMemberChange}
			onDeleteGroup={this.onDeleteGroup}
			onToggleGroup={this.onToggleGroup}
			onInputNameChange={this.onInputNameChange}
			onInputMembersChange={this.onInputMembersChange}
			onAddGroup={this.onAddGroup}
			onAddMemberToAllGroups={this.onAddMemberToAllGroups} />);
	}

	getGroups = () => {
		console.debug("RESTful: get groups");
		fetch("api/groups")
			.then(rsp => rsp.json())
			.then(groups => this.showGroups(groups))
			.catch(err => console.error("Members: getGroups", err));
	}

	showGroups = groups => {
		this.setState({
			members: create_members_model(groups)
		});
	}

	createGroup = (groupName, groupMembers) => {
		
		console.info("RESTful: create group "+groupName
			+" "+JSON.stringify(groupMembers));
		
		var postReq = {
			method: "POST",
			headers: {"Content-Type": "application/json"},
			body: JSON.stringify(groupMembers)
		};
		console.info("createGroup groupMembers: " + groupMembers);
		fetch("api/groups/"+groupName, postReq)
			.then(rsp => this.getGroups())
			.catch(err => console.error("Members: createGroup", err));
	}

	createManyGroups = groups => {
		console.info("RESTful: create many groups "+JSON.stringify(groups));
		var pendingReqs = groups.map(group => {
			var memberNames = group.members.map(function (elem) {
				return elem.name;
			});
			var postReq = {
				method: "POST",
				headers: {"Content-Type": "application/json"},
				body: JSON.stringify(memberNames)
			};

			console.info("members: " + JSON.stringify(memberNames));
			return fetch("api/groups/"+group.name, postReq);
		});
	
	
		Promise.all(pendingReqs)
			.then(() => this.getGroups())
			.catch(err => console.error("Members: createManyGroup", err));
	}

	deleteGroup = groupName => {
		console.info("RESTful: delete group "+groupName);
	
		var delReq = {
			method: "DELETE"
		};
		fetch("api/groups/"+groupName, delReq)
			.then(rsp => this.getGroups())
			.catch(err => console.error("Members: deleteGroup", err));
	}

	toggleGroup = groupName => {
		console.info("RESTful: toggle action " +groupName);
		fetch("api/groups/"+groupName+"?action=toggle")
			 .then(rsp => this.getGroups())
			 .catch(err => console.error("Members: toggleGroup", err));
	}

	onMemberChange = (memberName, groupName) => {
		var groupMembers = new Set(this.state.members.get_group_members(groupName));
		console.info("onMemberChange: "+ groupMembers.has(memberName));
		console.info("onMemberChange: groupMembers:" + groupMembers);
		if (groupMembers.has(memberName))
			groupMembers.delete(memberName);
		else
			groupMembers.add(memberName);
			;	
		this.createGroup(groupName, Array.from(groupMembers));
	}

	onDeleteGroup = groupName => {
		this.deleteGroup(groupName);
	}

	onToggleGroup = groupName => {
		this.toggleGroup(groupName);
	}

	onInputNameChange = value => {
		console.debug("Members: onInputNameChange", value);
		this.setState({inputName: value});
	}

	onInputMembersChange = value => {
		console.debug("Members: onInputMembersChange", value);
		this.setState({inputMembers: value});
	}

	onAddGroup = () => {
		var name = this.state.inputName;
		var members = this.state.inputMembers.split(',');
	
		this.createGroup(name, members);
	}

	onAddMemberToAllGroups = memberName => {
		var groups = [];
		for (var groupName of this.state.members.get_group_names()) {
			var groupMembers = new Set(this.state.members.get_group_members(groupName));
			groupMembers.add(memberName);
			groups.push({name: groupName, members: Array.from(groupMembers)});
		}
		this.createManyGroups(groups);
	}
}

// export
window.Members = Members;