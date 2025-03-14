import { Text, View } from "react-native";
import { Image } from 'react-native';

export default function Index() {
  return (
    <section
    className="flex flex-col items-center justify-center px-6 py-8 mx-auto min-h-screen lg:py-0" 
    style={{background: 'linear-gradient(135deg, #2C3E50 0%, #4A69BD 100%)'}}
>
  <div className="flex flex-col items-center justify-center px-6 py-8 mx-auto min-h-screen lg:py-0">
  <Image
        source={require('./images/coruja.png')}
        style={{
          width: 100,
          height: 100,
          position: 'absolute',
          top: 100,  // Distância do topo
          left: '50%', // Centraliza horizontalmente
          marginLeft: -50, // Ajusta a imagem para ficar centralizada corretamente (metade da largura da imagem)
        }}
      />
      <h1 className="flex items-center mb-4 text-2xl font-semibold text-black dark:text-black">
          NOCT    
      </h1>
      <div className="w-full bg-white border-2 border-gray-200 rounded-lg shadow-lg md:mt-0 sm:max-w-md xl:p-0">
          <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
              <h1 className="text-xl font-bold leading-tight tracking-tight text-gray-800 md:text-2xl dark:text-gray-800">
                  Entre na sua conta
              </h1>
              <form className="space-y-4 md:space-y-6" action="#">
                  <div>
                      <label htmlFor="email" className="block mb-2 text-sm font-medium text-gray-700 dark:text-gray-700">Email</label>
                      <input type="email" name="email" id="email" className="bg-gray-50 border border-gray-300 text-gray-900 rounded-lg focus:ring-indigo-500 focus:border-indigo-500 block w-full p-2.5 dark:bg-gray-50 dark:border-gray-300 dark:placeholder-gray-500 dark:text-gray-800 dark:focus:ring-indigo-500 dark:focus:border-indigo-500" 
                        placeholder="nome@exemplo.com" 
                        required 
                      />
                  </div>
                  <div>
                      <label htmlFor="password" className="block mb-2 text-sm font-medium text-gray-700 dark:text-gray-700">Senha</label>
                      <input type="password" name="password" id="password" placeholder="••••••••" className="bg-gray-50 border border-gray-300 text-gray-900 rounded-lg focus:ring-indigo-500 focus:border-indigo-500 block w-full p-2.5 dark:bg-gray-50 dark:border-gray-300 dark:placeholder-gray-500 dark:text-gray-800 dark:focus:ring-indigo-500 dark:focus:border-indigo-500" 
                        required 
                      />
                  </div>
                  <div className="flex items-center justify-between">
                      <div className="flex items-start">
                          <div className="flex items-center h-5">
                            <input id="remember" aria-describedby="remember" type="checkbox" className="w-4 h-4 border border-gray-300 rounded bg-gray-50 focus:ring-3 focus:ring-indigo-300 dark:bg-gray-100 dark:border-gray-300 dark:focus:ring-indigo-600 dark:ring-offset-gray-100" 
                              required
                            />
                          </div>
                          <div className="ml-2 text-sm">
                            <label htmlFor="remember" className="text-gray-600">Manter login</label>
                          </div>
                      </div>
                      <a href="#" className="text-sm font-medium text-indigo-600 hover:text-indigo-700 hover:underline">Esqueceu sua senha?</a>
                  </div>
                  <button
                    type="submit"
                    className="w-full text-white bg-indigo-600 hover:bg-indigo-700 focus:ring-4 focus:outline-none focus:ring-indigo-300 font-semibold rounded-lg text-sm px-6 py-3 text-center transition-all duration-100 ease-in-out transform hover:scale-100 active:scale-95"
                  >
                    Entrar
                  </button>
                  <p className="text-sm font-light text-gray-600">
                      Não possui uma conta ainda? <a href="#" className="font-medium text-indigo-600 hover:text-indigo-700 hover:underline">Cadastre-se</a>
                  </p>
              </form>
          </div>
      </div>
  </div>
</section>
  );
}
