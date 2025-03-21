export interface Operations {
  operacao: string;
  container: string;
  qtde_fotos: string;
}

const users: Operations[] = [
  {
    operacao: "#1234",
    container: "#C8234",
    qtde_fotos: "4",
  },
  {
    operacao: "#5678",
    container: "#D9123",
    qtde_fotos: "6",
  },
  {
    operacao: "#9101",
    container: "#A4567",
    qtde_fotos: "3",
  },
  {
    operacao: "#1121",
    container: "#B7890",
    qtde_fotos: "5",
  },
  {
    operacao: "#3141",
    container: "#E1357",
    qtde_fotos: "2",
  },
  {
    operacao: "#5161",
    container: "#F2468",
    qtde_fotos: "7",
  },
  {
    operacao: "#7181",
    container: "#G3692",
    qtde_fotos: "8",
  },
];

export default users;
