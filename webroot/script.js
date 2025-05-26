function testScript() {
    const result = document.getElementById("js-result");
    result.textContent = "✅ JavaScript is working!";
    result.style.color = "green";
}


function createMatch(){

    const p1 = {
            name: "Pedro",
            ELO: 1200,
            address: "localhost:8080"
        }

    const p2 = {
        name: "Álvaro",
        ELO: 1300,
        address: "localhost:8080"
    }

    searchAdversary(p1)
    .then(() => searchAdversary(p2))
    .then(() => window.location.href = "chess.html")

}

function searchAdversary(player){
    return fetch("/api/findMatch", {
        method: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(player)
    })
}