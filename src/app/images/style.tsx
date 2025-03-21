import { StyleSheet } from "react-native"

const sombra = StyleSheet.create({
  shadow: {
      shadowColor: "#000",
      shadowOffset: {
          width: 0,
          height: 30,  // Aumentado de 6 para 10
      },
      shadowOpacity: 1.2,  // Aumentado de 0.4 para 0.6
      shadowRadius: 28,  // Aumentado de 8 para 14

      elevation: 24,  // Aumentado de 12 para 24
  }
})

export default sombra