function testScript() {
    const result = document.getElementById("js-result");
    result.textContent = "✅ JavaScript is working!";
    result.style.color = "green";
}

// function createMatch(){
//     const player1 = {
//         name: "Pedro",
//         ELO: 1200,
//         address: "localhost:8080"
//     }

//     const player2 = {
//         name: "Álvaro",
//         ELO: 1300,
//         address: "localhost:8080"
//     }

//     fetch("/api/findMatch", {
//         method: "POST",
//         headers: {
//             'Content-Type': 'application/json'
//         },
//         body: JSON.stringify(player1)
//     })
//     .then(response => {
//         return response.text();
//     })
//     .then(data => console.log(`resposta do servidor ${data}`));
// }

// console.log("CARREGOU SCRIPT")