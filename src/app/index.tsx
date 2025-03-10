import { Text, View } from "react-native";
import { Image } from 'react-native';

export default function Index() {
  return (
    <View
      style={{
        backgroundColor:'#0e2e18', // Cor com base no modo
      }}
    >
      <Image
        source={require('./images/logo.png')}
        style={{
          width: 120,
          height: 120,
          position: 'absolute', 
          top: 50,  // Distância do topo
          left: '49%', // Centraliza horizontalmente
          marginLeft: -50, // Ajusta a imagem para ficar centralizada corretamente (metade da largura da imagem)
        }}
      />
  <div className="flex flex-col items-center justify-center px-6 py-8 mx-auto min-h-screen lg:py-0">
      <h1 className="flex items-center mb-6 text-2xl font-semibold text-gray-900 dark:text-white">
          ContainerView    
      </h1>
      <div className="w-full bg-white rounded-lg shadow dark:border md:mt-0 sm:max-w-md xl:p-0 dark:bg-lime-800 dark:border-lime-800">
          <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
              <h1 className="text-xl font-bold leading-tight tracking-tight text-gray-100 md:text-2xl dark:text-gray-100">
                  Entre na sua conta
              </h1>
              <form className="space-y-4 md:space-y-6" action="#">
                  <div>
                      <label htmlFor="email" className="block mb-2 text-sm font-medium text-gray-900 dark:text-white">Email</label>
                      <input type="email" name="email" id="email" className="bg-gray-50 border border-gray-300 text-gray-900 rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-lime-950 dark:border-lime-950 dark:placeholder-gray-100 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" 
  placeholder="nome@exemplo.com" 
  required 
/>
                  </div>
                  <div>
                      <label htmlFor="password" className="block mb-2 text-sm font-medium text-gray-900 dark:text-white">Senha</label>
                      <input type="password" name="password" id="password" placeholder="••••••••" className="bg-gray-50 border border-gray-300 text-gray-900 rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-lime-950 dark:border-lime-950 dark:placeholder-gray-100 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500" 
  required 
/>
                  </div>
                  <div className="flex items-center justify-between">
                      <div className="flex items-start">
                          <div className="flex items-center h-5">
                            <input id="remember" aria-describedby="remember" type="checkbox" className="w-4 h-4 border border-gray-300 rounded bg-gray-50 focus:ring-3 focus:ring-primary-300 dark:bg-gray-700 dark:border-gray-600 dark:focus:ring-primary-600 dark:ring-offset-gray-800" 
  required
/>
                          </div>
                          <div className="ml-2 text-sm">
                            <label htmlFor="remember" className="text-gray-500 dark:text-gray-300">Manter login</label>
                          </div>
                      </div>
                      <a href="#" className="text-sm font-medium text-primary-600 hover:underline dark:text-primary-500">Esqueceu sua senha?</a>
                  </div>
                  <button
  type="submit"
  className="w-full text-white bg-lime-950 hover:bg-lime-950 focus:ring-4 focus:outline-none font-semibold rounded-lg text-sm px-6 py-3 text-center transition-all duration-100 ease-in-out transform hover:scale-100 active:scale-95"
>
  Entrar
</button>
                  <p className="text-sm font-light text-gray-100 dark:text-gray-100">
                      Não possui uma conta ainda? <a href="#" className="font-medium text-primary-600 hover:underline dark:text-primary-500">Cadastre-se</a>
                  </p>
              </form>
          </div>
      </div>
  </div>
</View>
  );
}
