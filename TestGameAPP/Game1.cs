using System;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Input;

namespace TestGameAPP
{
    public class Game1 : Game
    {
        SensorData _data;
        readonly GraphicsDeviceManager graphics;
        SpriteBatch spriteBatch;

        Matrix projectionMatrix;
        Matrix viewMatrix;
        Matrix worldMatrix;

        VertexPositionColor[] triangleVertices;
        VertexBuffer vertexBuffer;
        IndexBuffer indexBuffer;
        BasicEffect effect;
        readonly ushort[] lineCubeIndices =
            {
                0,1,// передние линии
                1,2,
                2,3,
                3,0,

                4,5, // задние линии
                5,6,
                6,7,
                7,4,

                0,4,// боковые линии
                3,7,
                1,5,
                2,6
            };

        readonly ReceiverBluetoothService _receiverBluetoothService = new ReceiverBluetoothService();
        public Game1()
        {
            graphics = new GraphicsDeviceManager(this);
            _data = new SensorData
            {
                Type = 'a'
            };
            for (int i = 0; i < _data.Data.Length; i++)
            {
                _data.Data[i] = 0.0f;
            }
            Content.RootDirectory = "Content";
        }

        protected override void Initialize()
        {
            viewMatrix = Matrix.CreateLookAt(new Vector3(0, 0, 6), Vector3.Zero, Vector3.Up);

            projectionMatrix = Matrix.CreatePerspectiveFieldOfView(MathHelper.PiOver4,
                (float)Window.ClientBounds.Width / (float)Window.ClientBounds.Height,
                1, 100);

            worldMatrix = Matrix.CreateWorld(new Vector3(0f, 0f, 0f), new Vector3(0, 0, -1), Vector3.Up);

            // 8 вершин
            triangleVertices = new VertexPositionColor[8];
            triangleVertices[0] = new VertexPositionColor(new Vector3(-1, 1, 1), Color.Red);
            triangleVertices[1] = new VertexPositionColor(new Vector3(1, 1, 1), Color.Green);
            triangleVertices[2] = new VertexPositionColor(new Vector3(1, -1, 1), Color.Yellow);
            triangleVertices[3] = new VertexPositionColor(new Vector3(-1, -1, 1), Color.Blue);

            triangleVertices[4] = new VertexPositionColor(new Vector3(-1, 1, -1), Color.Red);
            triangleVertices[5] = new VertexPositionColor(new Vector3(1, 1, -1), Color.Green);
            triangleVertices[6] = new VertexPositionColor(new Vector3(1, -1, -1), Color.Yellow);
            triangleVertices[7] = new VertexPositionColor(new Vector3(-1, -1, -1), Color.Blue);

            vertexBuffer = new VertexBuffer(GraphicsDevice, typeof(VertexPositionColor),
                triangleVertices.Length, BufferUsage.None);
            vertexBuffer.SetData(triangleVertices);

            effect = new BasicEffect(GraphicsDevice)
            {
                VertexColorEnabled = true
            };

            // создаем буфер индексов
            indexBuffer = new IndexBuffer(graphics.GraphicsDevice, typeof(ushort), 24, BufferUsage.WriteOnly);
            indexBuffer.SetData<ushort>(lineCubeIndices);

            _receiverBluetoothService.Start((acc) => _data = acc);

            base.Initialize();
        }

        protected override void LoadContent()
        {
            spriteBatch = new SpriteBatch(GraphicsDevice);
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
                worldMatrix *= Matrix.CreateTranslation((float)Math.Round(_data.Data[0] * delta, 1), 0, 0);
                worldMatrix *= Matrix.CreateTranslation(0, (float)Math.Round(_data.Data[1] * delta, 1), 0);
                worldMatrix *= Matrix.CreateTranslation(0, 0, (float)Math.Round(_data.Data[2] * delta, 1));
            }
            else
            {
                worldMatrix *= Matrix.CreateRotationX(_data.Data[0] * delta);
                worldMatrix *= Matrix.CreateRotationY(_data.Data[1] * delta);
                worldMatrix *= Matrix.CreateRotationZ(_data.Data[2] * delta);
            }
            //worldMatrix *= Matrix.CreateRotationY(MathHelper.ToRadians(1));

            base.Update(gameTime);
        }
        protected override void Draw(GameTime gameTime)
        {
            GraphicsDevice.Clear(Color.CornflowerBlue);

            effect.World = worldMatrix;
            effect.View = viewMatrix;
            effect.Projection = projectionMatrix;
            GraphicsDevice.SetVertexBuffer(vertexBuffer);
            // устанавливаем буфер индексов
            GraphicsDevice.Indices = indexBuffer;
            foreach (EffectPass pass in effect.CurrentTechnique.Passes)
            {
                pass.Apply();
                // отрисовка примитива
                GraphicsDevice.DrawIndexedPrimitives(PrimitiveType.LineList, 0, 0, 8, 0, 12);
            }

            base.Draw(gameTime);
        }
    }
}