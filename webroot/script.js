function testScript() {
    const result = document.getElementById("js-result");
    result.textContent = "✅ JavaScript is working!";
    result.style.color = "green";
}

function createMatch() {
    // Definindo os jogadores disponíveis
    const players = {
        p1: {
            player: {
                name: "Pedro",
                ELO: 1200,
                address: "localhost:8080"
            }
        },
        p2: {
            player: {
                name: "Álvaro",
                ELO: 1300,
                address: "localhost:8080"
            }
        }
    };

    // Lógica melhorada para seleção do jogador
    let selectedPlayer;
    const storedPlayer = localStorage.getItem('currentPlayer');
    
    if (storedPlayer) {
        // Se já houver um jogador armazenado, alterna para o outro
        const parsedPlayer = JSON.parse(storedPlayer);
        selectedPlayer = parsedPlayer.player.name === 'Pedro' ? players.p2 : players.p1;
    } else {
        // Se não houver jogador armazenado, começa com Pedro
        selectedPlayer = players.p1;
    }

    // Armazena o jogador atual
    localStorage.setItem('currentPlayer', JSON.stringify(selectedPlayer));

    // Busca adversário e trata a resposta
    searchAdversary(selectedPlayer)
        .then(response => {
            if (!response.ok) {
                throw new Error('Erro na resposta do servidor');
            }
            return response.json();
        })
        .then(data => {
            console.log('Resposta do servidor:', data);
            // Armazena os dados da partida se necessário
            localStorage.setItem('matchData', JSON.stringify(data));
            // Redireciona apenas após receber a resposta
            window.location.href = "chess.html";
        })
        .catch(error => {
            console.error('Erro:', error);
            document.getElementById("js-result").textContent = "Erro: " + error.message;
            document.getElementById("js-result").style.color = "red";
        });
}

function searchAdversary(player) {
    return fetch("/api/findMatch", {
        method: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(player)
    });
}