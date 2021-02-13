const HOST = window.location.hostname
const WEB_SOCKET_END_POINT = HOST === "localhost" || HOST === "127.0.0.1" ?
	`ws://${HOST}:${window.location.port}/connect` : `wss://${HOST}/connect`;
const MOVE_COMMANDS = ['ADD', 'MAINTAIN', 'SUBTRACT']
let WEB_SOCKET;

window.addEventListener('beforeunload',  (e) => disconnect());

window.addEventListener('load', (e) => sessionStorage.clear());

const connect = () => {
	const player = getPlayer();
	if (player != null) {
		connectGame(player);
		return;
	}
	createPlayer()
		.then(result => result.json())
		.then(newPlayer => {
			savePlayer(newPlayer);
			connectWebSocket(newPlayer);
		}).catch((error) => console.error(`Error to create player: ${error}`));
}

const connectWebSocket = (newPlayer) => {
	WEB_SOCKET = Stomp.over(new WebSocket(WEB_SOCKET_END_POINT));
	WEB_SOCKET.connect({}, () => {
		WEB_SOCKET.subscribe(`/queue/player/${newPlayer.id}`,
			(message) => handleGameMessage(JSON.parse(message.body)));
		connectGame(newPlayer);
	}, (error) => {
		console.error("STOMP error " + error);
		disconnect();
	});
}

const disconnect = () => {
	if (WEB_SOCKET != null) {
		fetch(`/player/${getPlayer().id}/disconnect?gameId=${getGameId()}`,
			{ method: "PATCH" }).then(() => {
				sessionStorage.clear();
				WEB_SOCKET.disconnect();
				displayPlayAgainButton();
			}).catch((error) => console.error(`Error to disconnect player: ${error}`));
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
			const player = getPlayer();
			displayPlayContainer(player.isPlayingAutomatically)
			if (player.isPlayingAutomatically)
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

const getRandomMoveCommand = () => MOVE_COMMANDS[Math.floor(Math.random() * MOVE_COMMANDS.length)]

const enablePlayContainer = (enable) => {
	document.getElementById("move-select").disabled = !enable;
	document.getElementById("play-btn").disabled = !enable;
}

const addMessage = (message, color) => {
	const table = document.getElementById('messages-table')
		.getElementsByTagName('tbody')[0];
	const newRow = table.insertRow(table.rows.length);
	newRow.innerHTML = `<tr><td style="color: ${color}">${message}</td></tr>`;
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
}

const cleanGameLog = () => {
	document.getElementById('messages-table').getElementsByTagName('tbody')[0].innerHTML = "";
	document.getElementById("current-number").innerHTML = "";
}

const savePlayer = (player) => sessionStorage.setItem("player", JSON.stringify(player));

const getPlayer = () => JSON.parse(sessionStorage.getItem("player"));

const saveGameId = (gameId) => sessionStorage.setItem("gameId", gameId);

const getGameId = () => sessionStorage.getItem("gameId");

