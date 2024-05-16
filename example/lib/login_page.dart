import 'package:flutter/material.dart';
import 'package:frontegg/models/frontegg_state.dart';
import 'package:frontegg/utils.dart';

class LoginPage extends StatelessWidget {
  const LoginPage({super.key});

  @override
  Widget build(BuildContext context) {
    final frontegg = context.frontegg;

    return Scaffold(
      appBar: AppBar(
        title: const Text("Frontegg Example App"),
        centerTitle: true,
      ),
      body: Stack(
        children: [
          const Align(
            alignment: Alignment.topCenter,
            child: Padding(
              padding: EdgeInsets.only(top: 40),
              child: Text(
                'Not Authenticated',
                style: TextStyle(fontSize: 24),
              ),
            ),
          ),
          Align(
            alignment: Alignment.center,
            child: StreamBuilder<FronteggState>(
              stream: frontegg.onStateChanged,
              builder: (BuildContext context, AsyncSnapshot<FronteggState> snapshot) {
                if (snapshot.hasData) {
                  final state = snapshot.data!;
                  if (state.isLoading) {
                    return const CircularProgressIndicator();
                  } else if (!state.initializing && !state.isAuthenticated) {
                    return ElevatedButton(
                      child: const Text("Login"),
                      onPressed: () {
                        frontegg.login();
                      },
                    );
                  }
                }
                return const SizedBox();
              },
            ),
          )
        ],
      ),
    );
  }
}
