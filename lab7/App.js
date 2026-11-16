import React from 'react';
import { StyleSheet, Text, View } from 'react-native';

export default function App() {
  return (
    <View style={styles.container}>
      <View style={styles.card}>
        <Text style={styles.name}>
          Ramtin Loghmani
        </Text>

        <Text style={styles.studentId}>
          Student ID: 101595929
        </Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#D9D9D9',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 25,
  },

  card: {
    width: '90%',
    padding: 30,
    backgroundColor: '#1E1E1E',
    borderRadius: 20,
    alignItems: 'flex-start',
  },

  name: {
    fontSize: 38,
    fontWeight: 'bold',
    color: '#FF8C42',
  },

  studentId: {
    fontSize: 18,
    color: '#F5F5F5',
    marginTop: 12,
  },
});
