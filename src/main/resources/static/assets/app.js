let webSocket;

const connect = () => {
	createPlayer()
		.then(result => result.json())
		.then(newPlayer => {
			webSocket = Stomp.over(new WebSocket("ws://localhost:8000/connect"));
			webSocket.connect({}, function(frame) {
				subscribeGame(newPlayer.id);
				connectGame(newPlayer);
			}, function(error) {
				console.error("STOMP error " + error);
			});
		});
}

const createPlayer = () => {
	const createPlayerData = {
		name: document.getElementById("name").value,
		isPlayingAutomatically: document.getElementById("play-automatically").checked
	};

	return fetch("/player", {
		method: "POST",
		headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(createPlayerData)
	})
}

const subscribeGame = (playerId) => {
	webSocket.subscribe(`/queue/start/${playerId}`, (message) => addMessage(message.body, "green"));
	webSocket.subscribe(`/queue/wait/${playerId}`, (message) => addMessage(message.body, "blue"));
	webSocket.subscribe(`/queue/round/${playerId}`, (message) => addMessage(message.body, "green"));
	webSocket.subscribe(`/queue/win/${playerId}`, (message) => addMessage(message.body, "green"));
	webSocket.subscribe(`/queue/lose/${playerId}`, (message) => addMessage(message.body, "red"));
}

const connectGame = (player) => {
	webSocket.send("/game/connect", {}, player.id);
	displayPlayContainer(player.isPlayingAutomatically)
}

const disconnect = () => {
	if (webSocket != null)
		webSocket.close();
	document.getElementById("connect-container").classList.remove('hide');
	document.getElementById("play-container").classList.add('hide');
	document.getElementById("game-log-container").classList.add('hide');
}

const displayPlayContainer = (isPlayingAutomatically) => {
	document.getElementById("connect-container").classList.add('hide');
	document.getElementById("game-log-container").classList.remove('hide');
	if (isPlayingAutomatically)
		document.getElementById("play-container").classList.remove('hide');
	else
		document.getElementById("play-container").classList.remove('hide');
}

const addMessage = (message, color) => {
	const table = document.getElementById('messages-table').getElementsByTagName('tbody')[0];
	const newRow = table.insertRow(table.rows.length);
	newRow.innerHTML = `<tr><td style="color: ${color}">${message}</td></tr>`;
}
