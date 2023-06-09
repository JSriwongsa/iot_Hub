const btnClassAdd = "btn btn-primary btn-block";
const btnClassDel = "btn btn-danger btn-block";


/**
 * This is a stateless view showing the table header.
 */
function Header(props) {
	var ths = [];
	for (var groupName of props.groupNames) {
		var group_state = props.members.get_group_state(groupName);
		var btnClass = (group_state == "on") ? "btn-block btn-warning" : "btn-block btn-primary";
		ths.push(
			<th key={groupName}>
				<button className={btnClass}>{groupName}</button>
			</th>
		);
	}

	return (
		<thead>
			<tr>
				<th rowSpan="2" width="10%">Plug Members</th>
				<th colSpan={props.groupNames.length}>Groups</th>
			</tr>
			<tr>
				{ths}
			</tr>
		</thead>
	);
}

/**
 * This is a stateless view showing one row.
 */
function Row(props) {
	var members = props.members;
	console.info("Row() members: "+ JSON.stringify(members));
	var tds = members.get_group_names().map(groupName => {
		var onChange = () => props.onMemberChange(props.memberName, groupName);
		var checked = members.is_member_in_group(props.memberName, groupName);
		return (<td key={groupName}>
			<input type="checkbox"  onChange={onChange} checked={checked}/></td>);
	});

	var onAddClick = () => props.onAddMemberToAllGroups(props.memberName);
	return (
		<tr>
			<td><button className={btnClassAdd}>{props.memberName}</button></td>
			{tds}
		</tr>
	);
}

/**
 * This is a stateless view showing the row for delete groups.
 */
function DeleteGroupsRow(props) {
	var tds = props.groupNames.map(groupName => {
		var onClick = () => props.onDeleteGroup(groupName);
		return <td key={groupName}>
			<button className={btnClassDel} onClick={onClick}>Delete Group</button></td>;
	});

	return (
		<tr>
			<td></td>
			{tds}
			<td></td>
		</tr>
	);
}

function ToggleGroupsRow(props) {
	// allows the button to change color depending on if state is on or off
	var tds = props.groupNames.map(groupName => {
		var group_state = props.members.get_group_state(groupName);
		var btnClass = (group_state == "on") ? "btn-block btn-warning" : "btn-block btn-primary";
		var onClick = () => props.onToggleGroup(groupName);
		return <td key={groupName}>
			<button className={btnClass} onClick={onClick}>Toggle Group</button></td>;
	});

	return (
		<tr>
			<td></td>
			{tds}
			<td></td>
		</tr>
	);
}

/**
 * This is a stateless view showing inputs for add/replace groups.
 */
function AddGroup(props) {
	var onChangeName = event => props.onInputNameChange(event.target.value);
	var onChangeMembers = event => props.onInputMembersChange(event.target.value);

	return (
		<div>
			<label>Group Name</label>
			<input type="text" onChange={onChangeName} value={props.inputName}/>
			<label>Plug Members</label>
			<input type="text" onChange={onChangeMembers} value={props.inputMembers}
				size="60" placeholder="e.g. a,b,c"/>
			<button className="btn btn-primary" onClick={props.onAddGroup}>
				Add/Replace</button>
		</div>
	);
}

/**
 * This is a stateless view showing the table body.
 */
function Body(props) {
	var rows = props.members.get_member_names().map(memberName =>
		<Row key={memberName} memberName={memberName} members={props.members}
			onMemberChange={props.onMemberChange}
			onAddMemberToAllGroups={props.onAddMemberToAllGroups} />);

	return (
		<tbody>
			{rows}
			<DeleteGroupsRow groupNames={props.members.get_group_names()}
				onDeleteGroup={props.onDeleteGroup} />
			<ToggleGroupsRow groupNames={props.members.get_group_names()}
				members={props.members}
				onToggleGroup={props.onToggleGroup} />
			<tr><td colSpan="3">
				<AddGroup inputName={props.inputName} inputMembers={props.inputMembers}
					onInputNameChange={props.onInputNameChange}
					onInputMembersChange={props.onInputMembersChange}
					onAddGroup={props.onAddGroup} />
			</td></tr>
		</tbody>
	);
}

/**
 * This is a stateless view showing the whole members table.
 */
function MembersTable(props) {
	//console.info("MembersTable()");
	if (props.members.get_group_names().length == 0)
		return (
			<div>
				<div>There are no groups.</div>
				<AddGroup inputName={props.inputName} inputMembers={props.inputMembers}
					onInputNameChange={props.onInputNameChange}
					onInputMembersChange={props.onInputMembersChange}
					onAddGroup={props.onAddGroup} />
			</div>);

	return (
		<table className="table table-striped table-bordered">
			<Header groupNames={props.members.get_group_names()} members={props.members} />
			<Body members={props.members}
				inputName={props.inputName} inputMembers={props.inputMembers}
				onMemberChange={props.onMemberChange}
				onDeleteGroup={props.onDeleteGroup}
				onToggleGroup={props.onToggleGroup}
				onInputNameChange={props.onInputNameChange}
				onInputMembersChange={props.onInputMembersChange}
				onAddGroup={props.onAddGroup}
				onAddMemberToAllGroups={props.onAddMemberToAllGroups} />
		</table>);
}

//export
window.MembersTable = MembersTable;