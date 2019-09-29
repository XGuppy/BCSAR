using System;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Input;

namespace TestGameAPP
{
    public class Game1 : Game
    {
        GraphicsDeviceManager _graphics;
        SpriteBatch _spriteBatch;

        Matrix _projectionMatrix;
        Matrix _viewMatrix;
        Matrix _worldMatrix;
        SensorData _data;
        VertexPositionColor[] _triangleVertices;
        VertexBuffer _vertexBuffer;
        BasicEffect _effect;
        readonly ReceiverBluetoothService _receiverBluetoothService = new ReceiverBluetoothService();
        public Game1()
        {
            _graphics = new GraphicsDeviceManager(this);
            _data = new SensorData();
            _data.Type = 'a';
            for (int i = 0; i < _data.Data.Length; i++)
            {
                _data.Data[i] = 0.0f;
            }
            Content.RootDirectory = "Content";
        }

        protected override void Initialize()
        {
            _viewMatrix = Matrix.CreateLookAt(new Vector3(0, 0, 6), Vector3.Zero, Vector3.Up);

            _projectionMatrix = Matrix.CreatePerspectiveFieldOfView(MathHelper.PiOver4,
                (float)Window.ClientBounds.Width /
                (float)Window.ClientBounds.Height,
                1, 100);

            _worldMatrix = Matrix.CreateWorld(new Vector3(0f, 0f, 0f), new Vector3(0, 0, -1), Vector3.Up);

            // создаем набор вершин
            _triangleVertices = new VertexPositionColor[3];
            _triangleVertices[0] = new VertexPositionColor(new Vector3(0, 1, 0), Color.Red);
            _triangleVertices[1] = new VertexPositionColor(new Vector3(1, -1, 0), Color.Green);
            _triangleVertices[2] = new VertexPositionColor(new Vector3(-1, -1, 0), Color.Blue);

            // Создаем буфер вершин
            _vertexBuffer = new VertexBuffer(GraphicsDevice, typeof(VertexPositionColor),
                _triangleVertices.Length, BufferUsage.None);
            // Создаем BasicEffect
            _effect = new BasicEffect(GraphicsDevice) {VertexColorEnabled = true};
            // установка буфера вершин
            _vertexBuffer.SetData(_triangleVertices);
            _receiverBluetoothService.Start((acc) => _data = acc);
            base.Initialize();
        }

        protected override void LoadContent()
        {
            _spriteBatch = new SpriteBatch(GraphicsDevice);
        }

        protected override void UnloadContent()
        {

        }

        protected override void Update(GameTime gameTime)
        {
            if (GamePad.GetState(PlayerIndex.One).Buttons.Back == ButtonState.Pressed ||
                Keyboard.GetState().IsKeyDown(Keys.Escape))
                Exit();
            float delta = gameTime.ElapsedGameTime.Milliseconds / 1000.0f;
            if(_data.Type == 'a')
            {
                _worldMatrix *= Matrix.CreateTranslation((float)Math.Round(_data.Data[0] * delta, 1), 0, 0);
                _worldMatrix *= Matrix.CreateTranslation(0, (float)Math.Round(_data.Data[1] * delta, 1), 0);
                _worldMatrix *= Matrix.CreateTranslation(0, 0, (float)Math.Round(_data.Data[2] * delta, 1));
            }
            else
            {
                _worldMatrix *= Matrix.CreateRotationX(_data.Data[0] * delta);
                _worldMatrix *= Matrix.CreateRotationY(_data.Data[1] * delta);
                _worldMatrix *= Matrix.CreateRotationZ(_data.Data[2] * delta);
            }
            //worldMatrix *= Matrix.CreateRotationY(MathHelper.ToRadians(1));

            base.Update(gameTime);
        }
        protected override void Draw(GameTime gameTime)
        {
            GraphicsDevice.Clear(Color.CornflowerBlue);
            //установка матриц эффекта
            _effect.World = _worldMatrix;
            _effect.View = _viewMatrix;
            _effect.Projection = _projectionMatrix;
            // установка буфера вершин
            GraphicsDevice.SetVertexBuffer(_vertexBuffer);
            foreach (var pass in _effect.CurrentTechnique.Passes)
            {
                pass.Apply();

                GraphicsDevice.DrawUserPrimitives
                    (PrimitiveType.TriangleStrip, _triangleVertices, 0, 1);
            }

            base.Draw(gameTime);
        }
    }
}