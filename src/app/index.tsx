import React, { useState, useEffect } from 'react';
import { router } from 'expo-router';
import {
  Text,
  View,
  TextInput,
  TouchableOpacity,
  Image,
  SafeAreaView,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
  TouchableWithoutFeedback,
  Keyboard,
  Switch,
  StatusBar,
} from 'react-native';
import { cssInterop } from "nativewind";
import { LinearGradient } from 'expo-linear-gradient';

// Aplicando cssInterop para todos os componentes que vamos estilizar
cssInterop(Text, {
  className: {
    target: "style",
  },
});
cssInterop(View, {
  className: {
    target: "style",
  },
});
cssInterop(TextInput, {
  className: {
    target: "style",
  },
});
cssInterop(TouchableOpacity, {
  className: {
    target: "style",
  },
});
cssInterop(Image, {
  className: {
    target: "style",
  },
});
cssInterop(SafeAreaView, {
  className: {
    target: "style",
  },
});
cssInterop(ScrollView, {
  className: {
    target: "style",
  },
});
cssInterop(KeyboardAvoidingView, {
  className: {
    target: "style",
  },
});
cssInterop(LinearGradient, {
  className: {
    target: "style",
  },
});

export default function LoginScreen() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rememberLogin, setRememberLogin] = useState(false);

  // Configure status bar to match gradient background
  useEffect(() => {
    StatusBar.setBarStyle('light-content');
    if (Platform.OS === 'android') {
      StatusBar.setBackgroundColor('#5583D9');
      StatusBar.setTranslucent(true);
    }
  }, []);

  // Handle login navigation
  const handlelogs = () => {
    // You could add validation logic here
    router.push('/logs');
  };

  return (
    <View className="flex-1">
      <StatusBar />
      <LinearGradient
        colors={['#5583D9', '#5578D9']}
        start={{ x: 0, y: 0 }}
        end={{ x: 1, y: 1 }}
        className="flex-1"
      >
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        className="flex-1"
      >
        <TouchableWithoutFeedback onPress={Keyboard.dismiss}>
          <ScrollView className="flex-1">
            <SafeAreaView className="flex-1 items-center justify-center px-5 py-10">
              <Image
                source={require('./images/coruja.png')}
                className="w-24 h-24 mb-1 mt-20"
                resizeMode="contain"
              />
              <Text className="text-2xl font-bold text-black mb-8">NOCT</Text>
              
              <View className="w-full max-w-md bg-white rounded-lg shadow-lg px-6 py-6 mt-6">
                <Text className="text-xl font-bold text-gray-800 mb-5">
                  Entre na sua conta
                </Text>
                
                <View className="space-y-4">
                  <View>
                    <Text className="text-sm font-medium text-gray-700 mb-2">Email</Text>
                    <TextInput
                      className="bg-gray-50 border border-gray-300 text-gray-900 rounded-lg px-3 py-2.5 w-full"
                      placeholder="nome@exemplo.com"
                      placeholderTextColor="#A0AEC0"
                      value={email}
                      onChangeText={setEmail}
                      keyboardType="email-address"
                      autoCapitalize="none"
                    />
                  </View>
                  
                  <View>
                    <Text className="text-sm font-medium text-gray-700 mb-2 mt-5">Senha</Text>
                    <TextInput
                      className="bg-gray-50 border border-gray-300 text-gray-900 rounded-lg px-3 py-2.5 w-full"
                      placeholder="••••••••"
                      placeholderTextColor="#A0AEC0"
                      value={password}
                      onChangeText={setPassword}
                      secureTextEntry
                    />
                  </View>
                  
                  <View className="flex-row justify-between items-center mt-5">
                    <View className="flex-row items-center">
                      <Switch
                        value={rememberLogin}
                        onValueChange={setRememberLogin}
                        trackColor={{ false: "#E2E8F0", true: "#7F9CF5" }}
                        thumbColor={rememberLogin ? "#4C51BF" : "#f4f3f4"}
                      />
                      <Text className="ml-2 text-sm text-gray-600">Manter login</Text>
                    </View>
                    <TouchableOpacity>
                      <Text className="text-sm font-medium text-indigo-600">Esqueceu sua senha?</Text>
                    </TouchableOpacity>
                  </View>
                  
                  <TouchableOpacity
                    className="w-full bg-indigo-600 hover:bg-indigo-700 rounded-lg px-4 py-3 mt-5"
                    activeOpacity={0.8}
                    onPress={handlelogs}
                  >
                    <Text className="text-white text-center font-semibold">Entrar</Text>
                  </TouchableOpacity>
                  
                  <View className="flex-row justify-center flex-wrap mt-4">
                    <Text className="text-sm text-gray-600">
                      Não possui uma conta ainda?{' '}
                    </Text>
                    <TouchableOpacity >
                      <Text className="text-sm font-medium text-indigo-600">Cadastre-se</Text>
                    </TouchableOpacity>
                  </View>
                </View>
              </View>
            </SafeAreaView>
          </ScrollView>
        </TouchableWithoutFeedback>
      </KeyboardAvoidingView>
    </LinearGradient>
  </View>
  );
}