const WEB_SOCKET_END_POINT = `ws://${window.location.hostname}:${window.location.port}/connect`;
const MOVE_COMMANDS = ['ADD', 'MAINTAIN', 'SUBTRACT']
let WEB_SOCKET;

const connect = () => {
	if (getPlayer() != null) {
		WEB_SOCKET.disconnect();
		connectWebSocket(getPlayer());
		return;
	}
	createPlayer()
		.then(result => result.json())
		.then(newPlayer => {
			savePlayer(newPlayer);
			connectWebSocket(newPlayer);
		});
}

const connectWebSocket = (newPlayer) => {
	WEB_SOCKET = Stomp.over(new WebSocket(WEB_SOCKET_END_POINT));
	WEB_SOCKET.connect({}, () => {
		WEB_SOCKET.subscribe(`/queue/player/${newPlayer.id}`,
			(message) => handleGameMessage(JSON.parse(message.body)));
		WEB_SOCKET.subscribe(`/queue/player/${newPlayer.id}/error`,
			(message) => alert(`Error: ${message.body}`));
		connectGame(newPlayer);
	}, function(error) {
		console.error("STOMP error " + error);
	});
}

const disconnect = () => {
	if (WEB_SOCKET != null) {
		fetch(`/player/${getPlayer().id}/disconnect?gameId=${getGameId()}`, {method: "DELETE"})
			.then(() => {
				sessionStorage.clear();
				WEB_SOCKET.disconnect();
			});
		displayPlayAgainButton();
	}
}

const connectGame = (player) => {
	WEB_SOCKET.send("/game/connect", {}, player.id);
	cleanGameLog();
}

const startGame = () => {
	const player = getPlayer();
	const startGameData = {
		initialNumber: document.getElementById("initial-number").value,
		playerId: player.id,
		gameId: getGameId()
	}
	WEB_SOCKET.send("/game/start", {}, JSON.stringify(startGameData));
	displayPlayContainer(player.isPlayingAutomatically)
}

const handleGameMessage = (gameMessage) => {
	switch(gameMessage.type) {
		case "SUBSCRIBED":
			saveGameId(gameMessage.gameId);
			break;
		case "START":
			displayInitialNumberContainer();
			addMessage(gameMessage.message, "green")
			break;
		case "TURN":
			displayPlayContainer(getPlayer().isPlayingAutomatically)
			if (getPlayer().isPlayingAutomatically)
				executeMove()
			else
				enablePlayContainer(true);
			addMessage(gameMessage.message, "green")
			break;
		case "WAIT":
			displayPlayContainer(getPlayer().isPlayingAutomatically)
			addMessage(gameMessage.message, "blue")
			break;
		case "WON":
			displayPlayAgainButton();
			addMessage(gameMessage.message, "green")
			break;
		case "LOST": case "DRAW": case "PLAYER_DISCONNECTED":
			displayPlayAgainButton();
			addMessage(gameMessage.message, "red")
			break;
	}

	if (gameMessage.currentNumber != null)
		document.getElementById("current-number").innerHTML = `Current number: ${gameMessage.currentNumber}`;
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
	});
}

const executeMove = () => {
	const player = getPlayer();
	const moveData = {
		gameId: getGameId(),
		playerId: player.id,
		command: player.isPlayingAutomatically ? getRandomMoveCommand() :
			document.getElementById("move-select").value
	}
	WEB_SOCKET.send("/game/move", {}, JSON.stringify(moveData));
	enablePlayContainer(false);
}

const displayInitialNumberContainer = () => {
	document.getElementById("connect-container").classList.add('hide');
	document.getElementById("play-container").classList.add('hide');
	document.getElementById("initial-number-container").classList.remove('hide');
	document.getElementById("disconnect").classList.remove('hide');
}

const displayPlayContainer = (isPlayingAutomatically) => {
	document.getElementById("connect-container").classList.add('hide');
	document.getElementById("initial-number-container").classList.add('hide');
	document.getElementById("game-log-container").classList.remove('hide');
	document.getElementById("play-container").classList.remove('hide');
	document.getElementById("disconnect").classList.remove('hide');
	if (isPlayingAutomatically)
		document.getElementById("play-automatically-text").classList.remove('hide');
	else
		document.getElementById("play-form").classList.remove('hide');
}

const displayPlayAgainButton = () => {
	document.getElementById("initial-number-container").classList.add('hide');
	document.getElementById("play-container").classList.add('hide');
	document.getElementById("connect-form").classList.add('hide');
	document.getElementById("disconnect").classList.add('hide');
	document.getElementById("connect-container").classList.remove('hide');
	document.getElementById("play-again-btn").classList.remove('hide');
	document.getElementById("current-number").innerHTML = "";
}

const enablePlayContainer = (enable) => {
	document.getElementById("move-select").disabled = !enable;
	document.getElementById("play-btn").disabled = !enable;
}

const cleanGameLog = () => document.getElementById('messages-table').getElementsByTagName('tbody')[0].innerHTML = "";

const addMessage = (message, color) => {
	const table = document.getElementById('messages-table')
		.getElementsByTagName('tbody')[0];
	const newRow = table.insertRow(table.rows.length);
	newRow.innerHTML = `<tr><td style="color: ${color}">${message}</td></tr>`;
}

const savePlayer = (player) => sessionStorage.setItem("player", JSON.stringify(player));

const getPlayer = () => JSON.parse(sessionStorage.getItem("player"));

const saveGameId = (gameId) => sessionStorage.setItem("gameId", gameId);

const getGameId = () => sessionStorage.getItem("gameId");

const getRandomMoveCommand = () => MOVE_COMMANDS[Math.floor(Math.random() * MOVE_COMMANDS.length)]

window.addEventListener('beforeunload',  (e) => disconnect());

window.addEventListener('load', (e) => sessionStorage.clear());
