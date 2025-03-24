import React from "react";
import { TouchableOpacity, View, Text } from "react-native";
import { cssInterop } from 'nativewind';

cssInterop(View, {
  className: 'style',
});
cssInterop(TouchableOpacity, {
  className: 'style',
});

interface ListItemProps {
  data: {
    operacao: string;
    container: string;
    qtde_fotos: string;
  };
}

const ListItem: React.FC<ListItemProps> = ({ data }) => {
  return (
    <TouchableOpacity className="flex-row items-center p-4 bg-slate-200 border-b border-gray-200 active:opacity-70 w-96 h-16 rounded-lg">
      <View className="flex-1">
        <Text className="text-lg font-semibold text-gray-800">Operação {data.operacao}</Text>
        <View className="flex-row justify-between">
          <Text className="text-sm text-gray-500">Container {data.container}</Text>
          <Text className="text-sm text-gray-500">{data.qtde_fotos} fotos</Text>
        </View>
      </View>
    </TouchableOpacity>
  );
};

export default ListItem;