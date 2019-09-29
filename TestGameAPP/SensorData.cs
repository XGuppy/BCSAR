using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace TestGameAPP
{
    class SensorData
    {
        public char Type { get; set; }
        public float[] Data { get; private set; } = new float[3];
    }
}